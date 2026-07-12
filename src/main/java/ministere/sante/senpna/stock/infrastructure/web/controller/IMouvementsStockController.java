package ministere.sante.senpna.stock.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ministere.sante.senpna.stock.infrastructure.web.controller.implement.MouvementsStockController;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Mouvements de stock.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link MouvementsStockController}, qui n'en est que l'implémentation
 * métier — cf. règle « une classe, une raison de changer » (SOLID/SRP).
 * Les annotations de routage/binding sont portées ici. L'annotation de
 * sécurité ({@code @PreAuthorize}), ici définie au niveau méthode mais
 * identique sur les deux endpoints, reste volontairement sur
 * {@link MouvementsStockController}.
 * </p>
 *
 * <pre>
 * GET /api/mouvements-stock/{id}
 * GET /api/mouvements-stock?lotId=&medicamentId=&entrepotId=&typeMouvement=&sens=&utilisateurId=&dateDebut=&dateFin=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Mouvements de stock", description = """
    Consultation du journal des mouvements de stock — historique, filtrage et traçabilité \
    complète. Lecture seule : les mouvements sont créés exclusivement en conséquence d'une \
    entrée ou d'une sortie de stock (cf. le contrôleur Stocks), jamais directement.""")
@RequestMapping("/api/mouvements-stock")
public interface IMouvementsStockController {

  @Operation(summary = "Obtenir un mouvement de stock", description = """
      Retourne le détail complet d'un mouvement (type, sens, entrepôts source/destination, \
      lot, quantité, utilisateur, date). Un acteur PRA ne peut consulter qu'un mouvement \
      impliquant son propre entrepôt (comme source ou destination) ; un acteur PNA a une \
      portée illimitée.

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponse(responseCode = "200", description = "Mouvement récupéré")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant de consulter un mouvement d'un autre entrepôt (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Mouvement introuvable (MOUVEMENT_STOCK_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant du mouvement") @PathVariable UUID id);

  @Operation(summary = "Lister les mouvements de stock", description = """
      Recherche paginée sur le journal des mouvements. `lotId`/`medicamentId`/`utilisateurId` \
      filtrent sur une référence donnée. `typeMouvement` filtre sur l'une des valeurs de \
      `TypeMouvement` (ex. `ENTREE_ACHAT`, `SORTIE_TRANSFERT`, `PERTE`, `INVENTAIRE`...). \
      `sens` filtre sur `ENTREE` ou `SORTIE`. `dateDebut`/`dateFin` (format ISO-8601, ex. \
      `2026-01-01T00:00:00Z`) bornent la période sur `dateMouvement`. `sortBy`/`sortDirection` \
      pilotent le tri (défaut `dateMouvement`/`DESC`).

      `entrepotId` n'est réellement utilisable que par un acteur PNA : pour un acteur PRA, \
      il est **ignoré** et systématiquement remplacé par son propre entrepôt.

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponse(responseCode = "200", description = "Liste des mouvements récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "MOUVEMENTS_LISTED",
        "message": "Liste des mouvements de stock récupérée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "typeMouvement": "ENTREE_ACHAT",
            "sens": "ENTREE",
            "entrepotSourceId": null,
            "entrepotDestinationId": "8a2c...",
            "commandeId": null,
            "lotId": "3e7a...",
            "medicamentId": "6b1f...",
            "quantite": 500,
            "dateMouvement": "2026-01-05T09:00:00Z",
            "referenceDocument": "BR-2026-014",
            "motif": null,
            "utilisateurId": "9c4d...",
            "createdAt": "2026-01-05T09:00:00Z"
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 12,
          "totalItems": 231,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Type de mouvement ou sens invalide (TYPE_MOUVEMENT_INVALID / SENS_MOUVEMENT_INVALID)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Filtre sur un lot") @RequestParam(required = false) UUID lotId,
      @Parameter(description = "Filtre sur un médicament") @RequestParam(required = false) UUID medicamentId,
      @Parameter(description = "Filtre sur un entrepôt — pris en compte uniquement pour un acteur PNA ; ignoré et forcé à son propre entrepôt pour un acteur PRA") @RequestParam(required = false) UUID entrepotId,
      @Parameter(description = "Filtre sur le type de mouvement (cf. TypeMouvement)") @RequestParam(required = false) String typeMouvement,
      @Parameter(description = "Filtre sur le sens (ENTREE ou SORTIE)") @RequestParam(required = false) String sens,
      @Parameter(description = "Filtre sur l'utilisateur ayant réalisé le mouvement") @RequestParam(required = false) UUID utilisateurId,
      @Parameter(description = "Borne de début (ISO-8601), incluse") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateDebut,
      @Parameter(description = "Borne de fin (ISO-8601), incluse") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFin,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "dateMouvement") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}
