package ministere.sante.senpna.carriere.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import ministere.sante.senpna.carriere.infrastructure.web.dto.request.CreateOpportuniteCarriereRequest;
import ministere.sante.senpna.carriere.infrastructure.web.dto.request.UpdateOpportuniteCarriereRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Opportunités de carrière.
 *
 * <h3>Pièces jointes — flux Presigned URL, jamais géré par cette API</h3>
 * <p>
 * La fiche de poste (PDF préféré, image acceptée) n'est jamais envoyée à
 * cette API. Le front suit le même flux en 3 étapes que le reste du
 * projet (cf. {@code MediaController}) :
 * </p>
 * 
 * <pre>
 * 1. POST /api/medias/presigned-url   {mediaType: "FICHE_DE_POSTE", contentType, tailleBytes}
 *      → uploadUrl (signée, 10 min) + publicUrl
 * 2. PUT {uploadUrl}                  fichier binaire, direct front → stockage (S3/R2/MinIO)
 * 3. POST /api/opportunites           {..., ficheDePosteUrl: publicUrl}
 * </pre>
 * <p>
 * Le serveur applicatif ne reçoit et ne manipule jamais les octets du
 * fichier — uniquement l'URL publique qui en résulte.
 * </p>
 *
 * <pre>
 * POST   /api/opportunites                          {titre, nomEntreprise, description, lieu, typeContrat,
 *                                                      dateLimiteCandidature, dateDebut?, emailContact?,
 *                                                      ficheDePosteUrl?}
 * PUT    /api/opportunites/{id}                      idem
 * PATCH  /api/opportunites/{id}/publier
 * PATCH  /api/opportunites/{id}/mettre-en-cours
 * PATCH  /api/opportunites/{id}/cloturer
 * PATCH  /api/opportunites/{id}/brouillon
 * GET    /api/opportunites/{id}
 * GET    /api/opportunites?q=&typeContrat=&statut=&page=&size=&sortBy=&sortDirection=
 * GET    /api/opportunites/public/{id}
 * GET    /api/opportunites/public?q=&typeContrat=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Opportunités de carrière", description = """
                Gestion des offres d'emploi / opportunités de carrière : création, modification, cycle de vie \
                éditorial (brouillon → ouvert → en cours → clôturé, manuel ou automatique par dépassement de la \
                date limite de candidature) et consultation, y compris les endpoints publics `/public` et \
                `/public/{id}` ne nécessitant pas d'authentification.""")
@RequestMapping("/api/opportunites")
public interface IOpportunitesCarriereController {

        @Operation(summary = "Créer une opportunité de carrière (à l'état brouillon)", description = """
                        Crée une nouvelle offre, toujours à l'état `BROUILLON` — la publication est une décision \
                        distincte de la saisie (cf. `PATCH /{id}/publier`). `typeContrat` doit être l'une des \
                        valeurs `CDI`, `CDD`, `STAGE`, `FREELANCE`, `VOLONTARIAT`, `AUTRE`. `dateLimiteCandidature` \
                        est obligatoire et ne peut pas être postérieure à `dateDebut` si celle-ci est renseignée. \
                        `ficheDePosteUrl` est l'URL publique obtenue via le flux Presigned URL — voir la Javadoc \
                        du contrôleur, cette API ne reçoit jamais le fichier lui-même. L'auteur est déduit du \
                        token JWT.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "201", description = "Offre créée")
        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @PostMapping
        ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateOpportuniteCarriereRequest request);

        @Operation(summary = "Modifier le contenu d'une opportunité de carrière", description = """
                        Remplace le contenu éditorial de l'offre, quel que soit son statut courant. Le statut de \
                        publication n'est **pas** modifié par cet endpoint. `ficheDePosteUrl` doit être renvoyée \
                        telle quelle pour la conserver, remplacée par une nouvelle URL après un nouvel upload, ou \
                        omise/`null` pour la retirer.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Offre modifiée")
        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Offre introuvable (OPPORTUNITE_CARRIERE_NOT_FOUND)")
        @PutMapping("/{id}")
        ResponseEntity<Map<String, Object>> modifier(
                        @Parameter(description = "Identifiant de l'offre") @PathVariable UUID id,
                        @Valid @RequestBody UpdateOpportuniteCarriereRequest request);

        @Operation(summary = "Publier une opportunité de carrière", description = """
                        Fait passer l'offre à l'état `OUVERT`, ouvrant les candidatures. Échoue si la date limite \
                        de candidature est déjà dépassée. Opération idempotente.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Offre publiée")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Offre introuvable (OPPORTUNITE_CARRIERE_NOT_FOUND)")
        @ApiResponse(responseCode = "400", description = "Date limite de candidature dépassée (DATE_LIMITE_CANDIDATURE_INVALIDE)")
        @PatchMapping("/{id}/publier")
        ResponseEntity<Map<String, Object>> publier(
                        @Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);

        @Operation(summary = "Passer une opportunité de carrière en traitement", description = """
                        Fait passer l'offre à l'état `EN_COURS` — reste visible publiquement mais n'accepte plus \
                        de nouvelles candidatures. Opération idempotente.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Offre passée en traitement")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Offre introuvable (OPPORTUNITE_CARRIERE_NOT_FOUND)")
        @PatchMapping("/{id}/mettre-en-cours")
        ResponseEntity<Map<String, Object>> mettreEnCours(
                        @Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);

        @Operation(summary = "Clôturer une opportunité de carrière", description = """
                        Fait passer l'offre à l'état `CLOTURE` (clôture manuelle). Une offre est également \
                        clôturée automatiquement lorsque sa date limite de candidature est dépassée (cf. le job \
                        planifié de clôture automatique). Opération idempotente.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Offre clôturée")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Offre introuvable (OPPORTUNITE_CARRIERE_NOT_FOUND)")
        @PatchMapping("/{id}/cloturer")
        ResponseEntity<Map<String, Object>> cloturer(
                        @Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);

        @Operation(summary = "Remettre une opportunité de carrière en brouillon", description = """
                        Fait repasser l'offre à l'état `BROUILLON`, quel que soit son statut courant — utile pour \
                        retravailler une offre déjà publiée avant de la republier. Opération idempotente.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Offre remise en brouillon")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Offre introuvable (OPPORTUNITE_CARRIERE_NOT_FOUND)")
        @PatchMapping("/{id}/brouillon")
        ResponseEntity<Map<String, Object>> remettreEnBrouillon(
                        @Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);

        @Operation(summary = "Obtenir une opportunité de carrière (tous statuts)", description = """
                        Retourne le détail complet d'une offre quel que soit son statut — usage back-office.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Offre récupérée")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Offre introuvable (OPPORTUNITE_CARRIERE_NOT_FOUND)")
        @GetMapping("/{id}")
        ResponseEntity<Map<String, Object>> obtenir(
                        @Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);

        @Operation(summary = "Obtenir une opportunité de carrière publiée (accès public)", description = """
                        Endpoint public, sans authentification, destiné au site vitrine. Ne retourne l'offre que \
                        si elle est visible publiquement (`OUVERT` ou `EN_COURS`, date limite non dépassée) — \
                        sinon 404, même avec un identifiant correct.""")
        @ApiResponse(responseCode = "200", description = "Offre récupérée")
        @ApiResponse(responseCode = "404", description = "Offre introuvable ou non visible publiquement (OPPORTUNITE_CARRIERE_NOT_FOUND)")

        @GetMapping("/public/{id}")
        ResponseEntity<Map<String, Object>> obtenirPublic(
                        @Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);

        @Operation(summary = "Lister les opportunités de carrière (tous statuts, back-office)", description = """
                        Recherche paginée sur l'ensemble des offres, quel que soit leur statut. `q` recherche en \
                        texte libre (titre, entreprise, lieu), `typeContrat` et `statut` filtrent respectivement.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Liste récupérée")
        @ApiResponse(responseCode = "400", description = "typeContrat ou statut invalide")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @GetMapping
        ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String typeContrat,
                        @RequestParam(required = false) String statut,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection);

        @Operation(summary = "Lister les opportunités de carrière visibles publiquement (accès public)", description = """
                        Endpoint public, sans authentification. Ne retourne que les offres `OUVERT`/`EN_COURS` \
                        dont la date limite de candidature n'est pas dépassée — le paramètre `statut` n'existe \
                        volontairement pas ici.""")
        @ApiResponse(responseCode = "200", description = "Liste récupérée")
        @ApiResponse(responseCode = "400", description = "typeContrat invalide")
        @GetMapping("/public")
        ResponseEntity<Map<String, Object>> listerPublic(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String typeContrat,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}
