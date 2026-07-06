package ministere.sante.senpna.organisation.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.PrasController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreatePraRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.UpdatePraRequest;

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
 * Contrat API du contrôleur PRA.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link PrasController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link PrasController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST   /api/pras                  {code, nom, regionId, adresse?, telephone?}
 * PUT    /api/pras/{id}             {nom, adresse?, telephone?, regionId?}
 * PATCH  /api/pras/{id}/desactiver
 * PATCH  /api/pras/{id}/activer
 * GET    /api/pras/{id}
 * GET    /api/pras?q=&regionId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "PRA", description = """
    Gestion des Pharmacies Régionales d'Approvisionnement (PRA) — représentations régionales \
    de la PNA. La création reste réservée aux rôles nationaux PNA. La modification, \
    l'activation et la désactivation sont ouvertes aux rôles PRA, mais restreintes à la région \
    dont ils relèvent — un utilisateur d'une région ne peut pas gérer l'entrepôt d'une autre \
    région.""")
@RequestMapping("/api/pras")
public interface IPrasController {

  @Operation(summary = "Créer une PRA", description = """
      Crée une nouvelle PRA, active par défaut, rattachée à une région. Le `code` doit \
      être unique (2 à 30 caractères alphanumériques). La région référencée doit être \
      active.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA` — la création n'est pas ouverte aux \
      rôles PRA, contrairement à la modification et à l'activation/désactivation.""")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "PRA créée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "status": 201,
            "type": "PRA_CREATED",
            "message": "PRA créée avec succès",
            "timestamp": "2024-01-01T12:00:00Z",
            "results": {
              "id": "550e8400-e29b-41d4-a716-446655440000",
              "code": "PRA-DAKAR",
              "nom": "PRA de Dakar",
              "type": "PRA",
              "regionId": "3e7a...",
              "regionNom": "Dakar",
              "adresse": "Route de Rufisque",
              "telephone": "+221771234567",
              "responsableUserId": null,
              "actif": true,
              "createdAt": "2024-01-01T12:00:00Z",
              "updatedAt": "2024-01-01T12:00:00Z"
            }
          }
          """))),
      @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : code/nom/région manquant, code ou téléphone mal formé"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer une PRA"),
      @ApiResponse(responseCode = "409", description = "Ce code d'entrepôt est déjà utilisé (ENTREPOT_CODE_ALREADY_USED)"),
      @ApiResponse(responseCode = "422", description = "La région référencée est désactivée (REGION_INACTIVE)")
  })
  @PostMapping
  ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreatePraRequest request);

  @Operation(summary = "Modifier une PRA", description = """
      Modifie `nom`, `adresse`, `telephone` et, optionnellement, la région de \
      rattachement d'une PRA existante. Le statut actif/inactif n'est pas modifié ici — \
      utilisez `PATCH /{id}/activer` ou `/desactiver`.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA` — \
      un acteur PRA ne peut modifier que la PRA de sa propre région (`REGION_ACCESS_DENIED` \
      sinon), alors qu'un acteur PNA national peut modifier n'importe quelle PRA.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "PRA modifiée"),
      @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant de modifier la PRA d'une autre région (REGION_ACCESS_DENIED)"),
      @ApiResponse(responseCode = "404", description = "Entrepôt introuvable (ENTREPOT_NOT_FOUND)"),
      @ApiResponse(responseCode = "422", description = "L'entrepôt ciblé n'est pas de type PRA (INVALID_ENTREPOT_TYPE), ou la nouvelle région référencée est désactivée (REGION_INACTIVE)")
  })
  @PutMapping("/{id}")
  ResponseEntity<Map<String, Object>> modifier(
      @Parameter(description = "Identifiant de la PRA") @PathVariable UUID id,
      @Valid @RequestBody UpdatePraRequest request);

  @Operation(summary = "Désactiver une PRA", description = """
      Fait passer la PRA à l'état inactif. Opération idempotente.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA` — \
      même restriction de portée régionale que pour la modification.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "PRA désactivée"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant de désactiver la PRA d'une autre région (REGION_ACCESS_DENIED)"),
      @ApiResponse(responseCode = "404", description = "Entrepôt introuvable (ENTREPOT_NOT_FOUND)"),
      @ApiResponse(responseCode = "422", description = "L'entrepôt ciblé n'est pas de type PRA (INVALID_ENTREPOT_TYPE)")
  })
  @PatchMapping("/{id}/desactiver")
  ResponseEntity<Map<String, Object>> desactiver(
      @Parameter(description = "Identifiant de la PRA") @PathVariable UUID id);

  @Operation(summary = "Activer une PRA", description = """
      Fait passer la PRA à l'état actif. Opération idempotente.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA` — \
      même restriction de portée régionale que pour la modification.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "PRA activée"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant d'activer la PRA d'une autre région (REGION_ACCESS_DENIED)"),
      @ApiResponse(responseCode = "404", description = "Entrepôt introuvable (ENTREPOT_NOT_FOUND)"),
      @ApiResponse(responseCode = "422", description = "L'entrepôt ciblé n'est pas de type PRA (INVALID_ENTREPOT_TYPE)")
  })
  @PatchMapping("/{id}/activer")
  ResponseEntity<Map<String, Object>> activer(
      @Parameter(description = "Identifiant de la PRA") @PathVariable UUID id);

  @Operation(summary = "Obtenir une PRA", description = """
      Retourne le détail complet d'une PRA, active ou non. Ce endpoint réutilise la \
      consultation générique des entrepôts — il n'applique donc pas de vérification que \
      l'entrepôt consulté est bien de type PRA.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "PRA récupérée"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
      @ApiResponse(responseCode = "404", description = "Entrepôt introuvable (ENTREPOT_NOT_FOUND)")
  })
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant de la PRA") @PathVariable UUID id);

  @Operation(summary = "Lister les PRA", description = """
      Recherche paginée restreinte aux entrepôts de type `PRA` (la PNA centrale n'apparaît \
      jamais dans cette liste). `q` recherche en texte libre (code, nom). `regionId` \
      filtre sur une région donnée. `actif` filtre sur le statut. `sortBy`/`sortDirection` \
      pilotent le tri (défaut `createdAt`/`DESC`).

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Liste des PRA récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "status": 200,
            "type": "PRAS_LISTED",
            "message": "Liste des PRA récupérée",
            "timestamp": "2024-01-01T12:00:00Z",
            "results": [
              {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "code": "PRA-DAKAR",
                "nom": "PRA de Dakar",
                "type": "PRA",
                "regionId": "3e7a...",
                "regionNom": "Dakar",
                "adresse": "Route de Rufisque",
                "telephone": "+221771234567",
                "responsableUserId": "8a2c...",
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
      @Parameter(description = "Recherche texte libre sur le code ou le nom") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur une région") @RequestParam(required = false) UUID regionId,
      @Parameter(description = "Filtre sur le statut actif/inactif") @RequestParam(required = false) Boolean actif,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}
