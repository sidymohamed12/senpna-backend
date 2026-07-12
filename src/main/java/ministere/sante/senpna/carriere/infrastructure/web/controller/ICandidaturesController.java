package ministere.sante.senpna.carriere.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import ministere.sante.senpna.carriere.infrastructure.web.dto.request.SoumettreCandidatureRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Candidatures.
 *
 * <h3>Pièces jointes — flux Presigned URL, jamais géré par cette API</h3>
 * <p>
 * {@code POST /api/candidatures} est un endpoint <strong>public</strong>,
 * sans authentification — un candidat externe n'est pas un utilisateur du
 * système (cf. {@code SecurityConfig}). Le CV et la lettre de motivation
 * ne sont jamais envoyés à cette API : le candidat suit le même flux en 3
 * étapes que le reste du projet (cf. {@code MediaController}), via la
 * variante publique de l'endpoint de génération d'URL pré-signée :
 * </p>
 * 
 * <pre>
 * 1. POST /api/medias/presigned-url/public   {mediaType: "CV" | "LETTRE_DE_MOTIVATION", contentType, tailleBytes}
 *      → uploadUrl (signée, 10 min) + publicUrl
 *      (aucune authentification requise, mais mediaType strictement limité à CV / LETTRE_DE_MOTIVATION)
 * 2. PUT {uploadUrl}                         fichier binaire, direct candidat → stockage (S3/R2/MinIO)
 * 3. POST /api/candidatures                  {..., cvUrl: publicUrl, lettreMotivationUrl?: publicUrl}
 * </pre>
 * <p>
 * Le serveur applicatif ne reçoit et ne manipule jamais les octets du
 * fichier — uniquement les URLs publiques qui en résultent.
 * </p>
 *
 * <pre>
 * POST   /api/candidatures                          (public) {opportuniteId, civilite, nomComplet, email,
 *                                                              telephone, cvUrl, lettreMotivationUrl?,
 *                                                              messageComplementaire?, consentementRgpd}
 * GET    /api/candidatures/{id}
 * GET    /api/candidatures?opportuniteId=&q=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Candidatures", description = """
                Soumission et consultation des candidatures à une opportunité de carrière — formulaire de \
                postulation public (`POST /api/candidatures`) et revue back-office des candidatures reçues.""")
@RequestMapping("/api/candidatures")
public interface ICandidaturesController {

        @Operation(summary = "Soumettre une candidature à une opportunité de carrière", description = """
                        Endpoint public, sans authentification — destiné aux candidats externes. `civilite` doit \
                        valoir `M` ou `MME`. `email` est validé syntaxiquement (présence d'un domaine), \
                        `telephone` doit être au format international (ex : `+221771234567`). `cvUrl` est \
                        obligatoire, `lettreMotivationUrl` optionnelle — toutes deux obtenues via le flux \
                        Presigned URL public décrit dans la Javadoc du contrôleur ; cette API ne reçoit jamais \
                        les fichiers eux-mêmes. `consentementRgpd` doit valoir `true` — la candidature est \
                        refusée sinon. La candidature n'est acceptée que si l'offre est au statut effectif \
                        `OUVERT` (une offre `EN_COURS`, `CLOTURE` ou dont la date limite est dépassée refuse \
                        toute nouvelle candidature). Un accusé de réception est envoyé au candidat et une \
                        notification au contact RH de l'offre, de façon asynchrone et best-effort — un incident \
                        SMTP n'affecte jamais la confirmation de soumission renvoyée au candidat.""")
        @ApiResponse(responseCode = "201", description = "Candidature soumise")
        @ApiResponse(responseCode = "400", description = "Corps invalide, e-mail/téléphone invalide, ou consentement RGPD manquant (VALIDATION_ERROR / CONSENTEMENT_RGPD_REQUIS)")
        @ApiResponse(responseCode = "404", description = "Offre introuvable (OPPORTUNITE_CARRIERE_NOT_FOUND)")
        @ApiResponse(responseCode = "422", description = "L'offre n'accepte plus de nouvelles candidatures (OPPORTUNITE_FERMEE)")
        @PostMapping
        ResponseEntity<Map<String, Object>> soumettre(@Valid @RequestBody SoumettreCandidatureRequest request);

        @Operation(summary = "Obtenir une candidature", description = """
                        Retourne le détail complet d'une candidature — usage back-office (revue des candidatures).

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Candidature récupérée")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Candidature introuvable (CANDIDATURE_NOT_FOUND)")
        @GetMapping("/{id}")
        ResponseEntity<Map<String, Object>> obtenir(
                        @Parameter(description = "Identifiant de la candidature") @PathVariable UUID id);

        @Operation(summary = "Lister les candidatures", description = """
                        Recherche paginée des candidatures reçues, filtrable par offre ciblée (`opportuniteId`) \
                        et recherche texte libre (`q`, sur le nom complet et l'e-mail du candidat).

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Liste récupérée")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @GetMapping
        ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) UUID opportuniteId,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}
