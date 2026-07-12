package ministere.sante.senpna.organisation.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.RegionsController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreateRegionRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Régions.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link RegionsController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link RegionsController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST /api/regions        {code, nom}
 * GET  /api/regions
 * GET  /api/regions/{id}
 * </pre>
 */
@Tag(name = "Régions", description = """
    Référentiel des régions administratives — donnée de base à laquelle sont rattachées les \
    PRA et les structures sanitaires.""")
@RequestMapping("/api/regions")
public interface IRegionsController {

  @Operation(summary = "Créer une région", description = """
      Crée une nouvelle région administrative, active par défaut. Le `code` doit être \
      unique (2 à 20 caractères alphanumériques).

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "201", description = "Région créée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 201,
        "type": "REGION_CREATED",
        "message": "Région créée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "code": "DK",
          "nom": "Dakar",
          "actif": true,
          "createdAt": "2024-01-01T12:00:00Z",
          "updatedAt": "2024-01-01T12:00:00Z"
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : code ou nom manquant, code mal formé")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer une région")
  @ApiResponse(responseCode = "409", description = "Une région porte déjà ce code (REGION_CODE_ALREADY_USED)")
  @PostMapping
  ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateRegionRequest request);

  @Operation(summary = "Lister toutes les régions", description = """
      Retourne la liste complète des régions, actives et inactives — pas de pagination \
      ni de filtre : le référentiel des régions est de taille bornée et connue \
      (découpage administratif national).

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Liste des régions récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "REGIONS_LISTED",
        "message": "Liste des régions récupérée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "code": "DK",
            "nom": "Dakar",
            "actif": true,
            "createdAt": "2024-01-01T12:00:00Z",
            "updatedAt": "2024-01-01T12:00:00Z"
          }
        ]
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister();

  @Operation(summary = "Obtenir une région", description = """
      Retourne le détail complet d'une région, active ou inactive.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Région récupérée")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Région introuvable (REGION_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant de la région") @PathVariable UUID id);
}
