package ministere.sante.senpna.fournisseur.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.fournisseur.infrastructure.web.controller.implemment.FournisseursController;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.request.CreateFournisseurRequest;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.request.UpdateFournisseurRequest;

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
 * Contrat API du contrôleur Fournisseurs.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger (routes, corps de requête,
 * paramètres, exemples, codes de retour) séparément de
 * {@link FournisseursController}, qui n'en est que l'implémentation
 * métier — cf. règle « une classe, une raison de changer » (SOLID/SRP) :
 * documenter l'API et l'exposer via HTTP ne sont pas la même
 * responsabilité.
 * </p>
 *
 * <p>
 * Les annotations de routage et de binding ({@code @GetMapping},
 * {@code @RequestParam}, {@code @RequestBody}, {@code @Valid}...) sont
 * portées ici : Spring MVC les résout sur l'interface implémentée par le
 * bean contrôleur, il n'y a donc rien à répéter côté implémentation. Les
 * annotations de sécurité ({@code @PreAuthorize}) restent volontairement
 * sur {@link FournisseursController}, au plus près du code qu'elles
 * protègent.
 * </p>
 *
 * <pre>
 * POST   /api/fournisseurs                  {nom, adresse?, telephone?, email?, contactPrincipal?}
 * PUT    /api/fournisseurs/{id}              {nom, adresse?, telephone?, email?, contactPrincipal?}
 * PATCH  /api/fournisseurs/{id}/activer
 * PATCH  /api/fournisseurs/{id}/desactiver
 * GET    /api/fournisseurs/{id}
 * GET    /api/fournisseurs?q=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Fournisseurs", description = """
                Gestion des fournisseurs de médicaments (cf. doc. métier §5) : création, modification, \
                activation/désactivation et consultation. La PNA est seule habilitée à gérer les \
                fournisseurs — les achats fournisseurs ne sont réalisés que par la PNA.""")
@RequestMapping("/api/fournisseurs")
public interface IFournisseursController {

        @Operation(summary = "Créer un fournisseur", description = """
                        Crée un nouveau fournisseur, actif par défaut. Le nom doit être unique parmi les \
                        fournisseurs existants (insensible à la casse) — une tentative de doublon est \
                        rejetée plutôt que silencieusement fusionnée ou renommée.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Fournisseur créé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "status": 201,
                                          "type": "FOURNISSEUR_CREATED",
                                          "message": "Fournisseur créé avec succès",
                                          "timestamp": "2024-01-01T12:00:00Z",
                                          "results": {
                                            "id": "550e8400-e29b-41d4-a716-446655440000",
                                            "nom": "Sanofi Sénégal",
                                            "adresse": "Zone industrielle, Dakar",
                                            "telephone": "+221771234567",
                                            "email": "contact@sanofi.sn",
                                            "contactPrincipal": "M. Diop",
                                            "actif": true,
                                            "createdAt": "2024-01-01T12:00:00Z",
                                            "updatedAt": "2024-01-01T12:00:00Z"
                                          }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : nom manquant, téléphone non conforme au format international (+<indicatif><numéro>), email mal formé"),
                        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
                        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer un fournisseur"),
                        @ApiResponse(responseCode = "409", description = "Un fournisseur porte déjà ce nom (FOURNISSEUR_NOM_ALREADY_USED)")
        })
        @PostMapping
        ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateFournisseurRequest request);

        @Operation(summary = "Modifier un fournisseur", description = """
                        Remplace intégralement les informations d'un fournisseur existant (`nom`, \
                        `adresse`, `telephone`, `email`, `contactPrincipal`). Le statut actif/inactif \
                        n'est **pas** modifié par cet endpoint — utilisez `PATCH /{id}/activer` ou \
                        `/desactiver`. Le nom, s'il change, doit rester unique parmi les autres \
                        fournisseurs.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fournisseur modifié", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "status": 200,
                                          "type": "FOURNISSEUR_UPDATED",
                                          "message": "Fournisseur modifié avec succès",
                                          "timestamp": "2024-01-01T12:00:00Z",
                                          "results": {
                                            "id": "550e8400-e29b-41d4-a716-446655440000",
                                            "nom": "Sanofi Sénégal SA",
                                            "adresse": "Zone industrielle, Dakar",
                                            "telephone": "+221771234567",
                                            "email": "contact@sanofi.sn",
                                            "contactPrincipal": "M. Diop",
                                            "actif": true,
                                            "createdAt": "2024-01-01T12:00:00Z",
                                            "updatedAt": "2024-01-02T09:30:00Z"
                                          }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)"),
                        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
                        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à modifier un fournisseur"),
                        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable (FOURNISSEUR_NOT_FOUND)"),
                        @ApiResponse(responseCode = "409", description = "Un autre fournisseur porte déjà ce nom (FOURNISSEUR_NOM_ALREADY_USED)")
        })
        @PutMapping("/{id}")
        ResponseEntity<Map<String, Object>> modifier(
                        @Parameter(description = "Identifiant du fournisseur") @PathVariable UUID id,
                        @Valid @RequestBody UpdateFournisseurRequest request);

        @Operation(summary = "Activer un fournisseur", description = """
                        Fait passer le fournisseur à l'état actif — un fournisseur inactif ne peut pas \
                        être sélectionné pour de nouveaux achats (cf. règles métier d'approvisionnement). \
                        Opération idempotente : appeler cet endpoint sur un fournisseur déjà actif ne \
                        renvoie pas d'erreur.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fournisseur activé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "status": 200,
                                          "type": "FOURNISSEUR_ACTIVATED",
                                          "message": "Fournisseur activé avec succès",
                                          "timestamp": "2024-01-01T12:00:00Z",
                                          "results": { "id": "550e8400-e29b-41d4-a716-446655440000", "actif": true }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
                        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à activer un fournisseur"),
                        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable (FOURNISSEUR_NOT_FOUND)")
        })
        @PatchMapping("/{id}/activer")
        ResponseEntity<Map<String, Object>> activer(
                        @Parameter(description = "Identifiant du fournisseur") @PathVariable UUID id);

        @Operation(summary = "Désactiver un fournisseur", description = """
                        Fait passer le fournisseur à l'état inactif — il n'est plus sélectionnable pour de \
                        nouveaux achats mais reste consultable, notamment pour l'historique des lots déjà \
                        réceptionnés qui lui sont rattachés. Opération idempotente.

                        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fournisseur désactivé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "status": 200,
                                          "type": "FOURNISSEUR_DEACTIVATED",
                                          "message": "Fournisseur désactivé avec succès",
                                          "timestamp": "2024-01-01T12:00:00Z",
                                          "results": { "id": "550e8400-e29b-41d4-a716-446655440000", "actif": false }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
                        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à désactiver un fournisseur"),
                        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable (FOURNISSEUR_NOT_FOUND)")
        })
        @PatchMapping("/{id}/desactiver")
        ResponseEntity<Map<String, Object>> desactiver(
                        @Parameter(description = "Identifiant du fournisseur") @PathVariable UUID id);

        @Operation(summary = "Obtenir un fournisseur", description = """
                        Retourne le détail complet d'un fournisseur, actif ou non.

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `PHARMACIEN_PNA` ou \
                        `MAGASINIER_PNA`.""")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Fournisseur récupéré"),
                        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
                        @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
                        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable (FOURNISSEUR_NOT_FOUND)")
        })
        @GetMapping("/{id}")
        ResponseEntity<Map<String, Object>> obtenir(
                        @Parameter(description = "Identifiant du fournisseur") @PathVariable UUID id);

        @Operation(summary = "Lister les fournisseurs", description = """
                        Recherche paginée sur l'ensemble des fournisseurs. `q` recherche en texte libre \
                        (nom, contact principal). `actif` filtre sur le statut (`true`/`false`) ; omis, \
                        les fournisseurs actifs et inactifs sont tous deux retournés. `sortBy`/`sortDirection` \
                        pilotent le tri (ex. `createdAt`/`DESC`).

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `PHARMACIEN_PNA` ou \
                        `MAGASINIER_PNA`.""")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Liste des fournisseurs récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                          "status": 200,
                                          "type": "FOURNISSEURS_LISTED",
                                          "message": "Liste des fournisseurs récupérée",
                                          "timestamp": "2024-01-01T12:00:00Z",
                                          "results": [
                                            {
                                              "id": "550e8400-e29b-41d4-a716-446655440000",
                                              "nom": "Sanofi Sénégal",
                                              "adresse": "Zone industrielle, Dakar",
                                              "telephone": "+221771234567",
                                              "email": "contact@sanofi.sn",
                                              "contactPrincipal": "M. Diop",
                                              "actif": true,
                                              "createdAt": "2024-01-01T12:00:00Z",
                                              "updatedAt": "2024-01-01T12:00:00Z"
                                            }
                                          ],
                                          "pagination": {
                                            "currentPage": 0,
                                            "totalPages": 2,
                                            "totalItems": 27,
                                            "first": true,
                                            "last": false
                                          }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
                        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        })
        @GetMapping
        ResponseEntity<Map<String, Object>> lister(
                        @Parameter(description = "Recherche texte libre sur le nom ou le contact principal") @RequestParam(required = false) String q,
                        @Parameter(description = "Filtre sur le statut actif/inactif") @RequestParam(required = false) Boolean actif,
                        @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
                        @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
                        @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}