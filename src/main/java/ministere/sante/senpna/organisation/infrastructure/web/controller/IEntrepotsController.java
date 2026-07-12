package ministere.sante.senpna.organisation.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.EntrepotsController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Entrepôts.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link EntrepotsController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici. L'annotation de
 * sécurité ({@code @PreAuthorize}), ici définie au niveau classe (même
 * périmètre de rôles pour les deux endpoints), reste volontairement sur
 * {@link EntrepotsController}.
 * </p>
 *
 * <pre>
 * GET /api/entrepots/{id}
 * GET /api/entrepots?q=&type=&regionId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Entrepôts", description = """
    Consultation générique des entrepôts, tous types confondus (PRA et PNA centrale) — utilisé \
    notamment pour peupler le choix d'entrepôt lors de la création d'un compte utilisateur par \
    un acteur national. Pour la gestion complète du cycle de vie d'une PRA \
    (création/modification/désactivation), voir le contrôleur PRA.""")
@RequestMapping("/api/entrepots")
public interface IEntrepotsController {

  @Operation(summary = "Obtenir un entrepôt", description = """
      Retourne le détail complet d'un entrepôt (PRA ou PNA centrale), actif ou non.""")
  @ApiResponse(responseCode = "200", description = "Entrepôt récupéré")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Entrepôt introuvable (ENTREPOT_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant de l'entrepôt") @PathVariable UUID id);

  @Operation(summary = "Lister les entrepôts", description = """
      Recherche paginée sur l'ensemble des entrepôts, tous types confondus. `q` recherche \
      en texte libre (code, nom). `type` filtre sur `PNA_CENTRAL` ou `PRA`. `regionId` \
      filtre sur une région donnée (n'a de sens que pour les entrepôts de type `PRA`, la \
      PNA centrale n'étant rattachée à aucune région). `actif` filtre sur le statut. \
      `sortBy`/`sortDirection` pilotent le tri (défaut `createdAt`/`DESC`).""")
  @ApiResponse(responseCode = "200", description = "Liste des entrepôts récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "ENTREPOTS_LISTED",
        "message": "Liste des entrepôts récupérée",
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
          "totalItems": 15,
          "first": true,
          "last": true
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Recherche texte libre sur le code ou le nom") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur le type d'entrepôt (PNA_CENTRAL ou PRA)") @RequestParam(required = false) TypeEntrepot type,
      @Parameter(description = "Filtre sur une région (pertinent uniquement pour les entrepôts de type PRA)") @RequestParam(required = false) UUID regionId,
      @Parameter(description = "Filtre sur le statut actif/inactif") @RequestParam(required = false) Boolean actif,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}
