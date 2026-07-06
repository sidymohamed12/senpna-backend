package ministere.sante.senpna.medicament.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.medicament.infrastructure.web.controller.implement.FamillesController;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateFamilleRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateFamilleRequest;

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
 * Contrat API du contrôleur Familles thérapeutiques.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger (routes, corps de requête,
 * paramètres, exemples, codes de retour) séparément de
 * {@link FamillesController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici : Spring MVC les résout
 * sur l'interface implémentée par le bean contrôleur. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link FamillesController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST   /api/familles                   {code, libelle, description?}
 * PUT    /api/familles/{id}               {libelle, description?}
 * PATCH  /api/familles/{id}/archiver
 * PATCH  /api/familles/{id}/desarchiver
 * GET    /api/familles/{id}
 * GET    /api/familles?q=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Familles thérapeutiques", description = """
        Gestion du référentiel des familles thérapeutiques, utilisé par le catalogue des \
        médicaments pour le classement et les statistiques de consommation par famille.""")
@RequestMapping("/api/familles")
public interface IFamillesController {

    @Operation(summary = "Créer une famille thérapeutique", description = """
            Crée une nouvelle famille, active par défaut. Le `code` doit être unique parmi les \
            familles existantes et respecter le format alphanumérique attendu (2 à 30 caractères, \
            lettres/chiffres/`_`/`-`).

            Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Famille créée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 201,
                      "type": "FAMILLE_CREATED",
                      "message": "Famille créée avec succès",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "code": "ANTALG",
                        "libelle": "Antalgiques",
                        "description": "Médicaments contre la douleur",
                        "actif": true,
                        "createdAt": "2024-01-01T12:00:00Z",
                        "updatedAt": "2024-01-01T12:00:00Z"
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : code manquant ou mal formé, libellé manquant"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer une famille"),
            @ApiResponse(responseCode = "409", description = "Une famille porte déjà ce code (FAMILLE_CODE_ALREADY_USED)")
    })
    @PostMapping
    ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateFamilleRequest request);

    @Operation(summary = "Modifier une famille thérapeutique", description = """
            Modifie `libelle` et `description` d'une famille existante. Le `code`, identifiant \
            stable de la famille, n'est **pas** modifiable par cet endpoint. Le statut \
            actif/inactif n'est pas non plus modifié ici — utilisez `PATCH /{id}/archiver` ou \
            `/desarchiver`.

            Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Famille modifiée"),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "Rôle non autorisé à modifier une famille"),
            @ApiResponse(responseCode = "404", description = "Famille introuvable (FAMILLE_NOT_FOUND)")
    })
    @PutMapping("/{id}")
    ResponseEntity<Map<String, Object>> modifier(
            @Parameter(description = "Identifiant de la famille") @PathVariable UUID id,
            @Valid @RequestBody UpdateFamilleRequest request);

    @Operation(summary = "Archiver une famille thérapeutique", description = """
            Retire la famille du référentiel actif — elle ne peut alors plus être rattachée à \
            un nouveau médicament (cf. `FAMILLE_INACTIVE` sur le contrôleur Médicaments), mais \
            reste consultable pour les médicaments qui la référencent déjà. Opération \
            idempotente.

            Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Famille archivée"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
            @ApiResponse(responseCode = "404", description = "Famille introuvable (FAMILLE_NOT_FOUND)")
    })
    @PatchMapping("/{id}/archiver")
    ResponseEntity<Map<String, Object>> archiver(
            @Parameter(description = "Identifiant de la famille") @PathVariable UUID id);

    @Operation(summary = "Désarchiver une famille thérapeutique", description = """
            Réintègre la famille dans le référentiel actif — elle redevient sélectionnable \
            pour un nouveau médicament. Opération idempotente.

            Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Famille désarchivée"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
            @ApiResponse(responseCode = "404", description = "Famille introuvable (FAMILLE_NOT_FOUND)")
    })
    @PatchMapping("/{id}/desarchiver")
    ResponseEntity<Map<String, Object>> desarchiver(
            @Parameter(description = "Identifiant de la famille") @PathVariable UUID id);

    @Operation(summary = "Obtenir une famille thérapeutique", description = """
            Retourne le détail complet d'une famille, active ou archivée.

            Rôle requis : acteurs PNA, PRA ou `GESTIONNAIRE_STRUCTURE`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Famille récupérée"),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
            @ApiResponse(responseCode = "404", description = "Famille introuvable (FAMILLE_NOT_FOUND)")
    })
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> obtenir(
            @Parameter(description = "Identifiant de la famille") @PathVariable UUID id);

    @Operation(summary = "Lister les familles thérapeutiques", description = """
            Recherche paginée sur l'ensemble des familles. `q` recherche en texte libre \
            (code, libellé). `actif` filtre sur le statut ; omis, actives et archivées sont \
            toutes deux retournées. `sortBy`/`sortDirection` pilotent le tri (défaut \
            `libelle`/`ASC`).

            Rôle requis : acteurs PNA, PRA ou `GESTIONNAIRE_STRUCTURE`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des familles récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "FAMILLES_LISTED",
                      "message": "Liste des familles récupérée",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": [
                        {
                          "id": "550e8400-e29b-41d4-a716-446655440000",
                          "code": "ANTALG",
                          "libelle": "Antalgiques",
                          "description": "Médicaments contre la douleur",
                          "actif": true,
                          "createdAt": "2024-01-01T12:00:00Z",
                          "updatedAt": "2024-01-01T12:00:00Z"
                        }
                      ],
                      "pagination": {
                        "currentPage": 0,
                        "totalPages": 1,
                        "totalItems": 14,
                        "first": true,
                        "last": true
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
    })
    @GetMapping
    ResponseEntity<Map<String, Object>> lister(
            @Parameter(description = "Recherche texte libre sur le code ou le libellé") @RequestParam(required = false) String q,
            @Parameter(description = "Filtre sur le statut actif/archivé") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "libelle") String sortBy,
            @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "ASC") String sortDirection);
}