package ministere.sante.senpna.stock.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.stock.infrastructure.web.controller.implement.StocksController;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.DefinirSeuilAlerteRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.EntreeStockRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.LibererReservationRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ReserverStockFefoRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ReserverStockRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.SortieStockRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Stocks.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link StocksController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link StocksController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST   /api/stocks/entrees                     {entrepotId, lotId, quantite, typeMouvement, commandeId?, referenceDocument?, motif?}
 * POST   /api/stocks/sorties                      {entrepotId, lotId, quantite, typeMouvement, depuisReservation, entrepotDestinationId?, commandeId?, referenceDocument?, motif?}
 * POST   /api/stocks/reservations                 {entrepotId, lotId, quantite, commandeId?}
 * POST   /api/stocks/reservations/fefo             {entrepotId, medicamentId, quantiteDemandee, commandeId?}
 * POST   /api/stocks/reservations/liberer          {entrepotId, lotId, quantite, commandeId?, motif?}
 * PATCH  /api/stocks/{id}/seuil-alerte             {seuilAlerte}
 * GET    /api/stocks/{id}
 * GET    /api/stocks?entrepotId=&lotId=&medicamentId=&ruptureUniquement=&seuilAtteintUniquement=&page=&size=&sortBy=&sortDirection=
 * GET    /api/stocks/alertes/rupture?entrepotId=&medicamentId=&seuilAtteintUniquement=&page=&size=
 * </pre>
 */
@Tag(name = "Stocks", description = """
    Gestion du stock — entrées, sorties, réservations et consultation. Chaque opération mutative \
    enregistre le mouvement de stock correspondant dans la même transaction applicative \
    (cf. le contrôleur Mouvements de stock pour leur consultation).""")
@RequestMapping("/api/stocks")
public interface IStocksController {

  @Operation(summary = "Enregistrer une entrée en stock", description = """
      Augmente la quantité disponible de la ligne de stock (entrepôt, lot) — créée à zéro \
      si elle n'existe pas encore — et enregistre le mouvement correspondant. \
      `typeMouvement` doit être l'une des valeurs de `TypeMouvement` compatibles avec un \
      sens `ENTREE` (ex. `ENTREE_ACHAT`, `ENTREE_TRANSFERT`, `RETOUR`, `AJUSTEMENT`, \
      `INVENTAIRE`, `DON`).

      Rôle requis : `ADMIN_PNA`, `MAGASINIER_PNA`, `ADMIN_PRA` ou `MAGASINIER_PRA` — \
      l'entrepôt ciblé (`entrepotId`) doit être le sien, qu'il soit acteur PNA ou PRA \
      (contrairement à la lecture, l'écriture n'est jamais élargie à un autre entrepôt).""")
  @ApiResponse(responseCode = "201", description = "Entrée en stock enregistrée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 201,
        "type": "STOCK_ENTREE_ENREGISTREE",
        "message": "Entrée en stock enregistrée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "entrepotId": "8a2c...",
          "lotId": "3e7a...",
          "medicamentId": "6b1f...",
          "quantiteDisponible": 500,
          "quantiteReservee": 0,
          "quantiteDisponibleALaVente": 500,
          "quantiteEnCommande": 0,
          "seuilAlerte": 100,
          "enRupture": false,
          "seuilAtteint": false,
          "createdAt": "2024-01-01T12:00:00Z",
          "updatedAt": "2024-01-01T12:00:00Z"
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : entrepôt/lot/quantité/type de mouvement manquant, quantité non strictement positive, ou type de mouvement invalide (TYPE_MOUVEMENT_INVALID)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou entrepôt ciblé différent du sien (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Lot ou entrepôt introuvable (LOT_NOT_FOUND / ENTREPOT_NOT_FOUND)")
  @PostMapping("/entrees")
  ResponseEntity<Map<String, Object>> entrer(@Valid @RequestBody EntreeStockRequest request);

  @Operation(summary = "Enregistrer une sortie de stock", description = """
      Diminue la quantité disponible d'une ligne de stock et enregistre le mouvement \
      correspondant. Les sorties d'expédition (`SORTIE_TRANSFERT`, `SORTIE_STRUCTURE`) \
      exigent un lot disponible (`ACTIF`, non expiré) ; les sorties correctives (`PERTE`, \
      `CASSE`, `VOL`, `PEREMPTION`, `AJUSTEMENT`, `INVENTAIRE`, `DON`) s'appliquent quel \
      que soit le statut du lot — elles servent précisément à retirer du stock physique \
      un lot bloqué ou expiré.

      `depuisReservation=true` consomme la quantité déjà réservée plutôt que la quantité \
      disponible brute (ex. expédition consécutive à une commande déjà réservée) ; sinon \
      la quantité réservée reste protégée et ne peut jamais être entamée par une sortie \
      directe.

      Rôle requis : `ADMIN_PNA`, `MAGASINIER_PNA`, `ADMIN_PRA` ou `MAGASINIER_PRA` — \
      l'entrepôt source doit être le sien ; l'entrepôt destination d'un transfert \
      appartient à l'acteur qui traitera sa propre réception, de son côté.""")
  @ApiResponse(responseCode = "201", description = "Sortie de stock enregistrée")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR), ou type de mouvement invalide (TYPE_MOUVEMENT_INVALID)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou entrepôt source différent du sien (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Lot, entrepôt, ou ligne de stock introuvable (LOT_NOT_FOUND / ENTREPOT_NOT_FOUND / STOCK_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "Lot bloqué ou expiré pour une sortie d'expédition (LOT_NOT_AVAILABLE), quantité disponible insuffisante (STOCK_INSUFFICIENT), ou quantité réservée insuffisante si depuisReservation=true (RESERVATION_INSUFFICIENT)")
  @PostMapping("/sorties")
  ResponseEntity<Map<String, Object>> sortir(@Valid @RequestBody SortieStockRequest request);

  @Operation(summary = "Réserver une quantité sur un lot précis", description = """
      Réserve une quantité sur une ligne de stock (entrepôt, lot) déjà identifiée — \
      utilisée lorsque le lot a été sélectionné explicitement (ex. par un pharmacien lors \
      du contrôle des lots), par opposition à la réservation automatique FEFO \
      (`POST /reservations/fefo`). La quantité réservée n'est pas retirée du stock \
      disponible mais protégée d'une sortie directe non adossée à une réservation.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA` — \
      l'entrepôt ciblé doit être le sien.""")
  @ApiResponse(responseCode = "200", description = "Quantité réservée")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou entrepôt ciblé différent du sien (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Lot ou ligne de stock introuvable (LOT_NOT_FOUND / STOCK_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "Lot bloqué ou expiré (LOT_NOT_AVAILABLE), ou quantité disponible insuffisante (STOCK_INSUFFICIENT)")
  @PostMapping("/reservations")
  ResponseEntity<Map<String, Object>> reserver(@Valid @RequestBody ReserverStockRequest request);

  @Operation(summary = "Réserver automatiquement selon la règle FEFO", description = """
      Réserve la quantité demandée pour un médicament en consommant automatiquement les \
      lots `ACTIF` non expirés de l'entrepôt par date d'expiration croissante (First \
      Expired, First Out), jusqu'à satisfaction complète.

      Opération tout-ou-rien : si la quantité demandée ne peut être entièrement \
      satisfaite par l'ensemble des lots disponibles dans l'entrepôt, **aucune réservation \
      partielle n'est conservée** — la transaction est intégralement annulée et une erreur \
      est renvoyée.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA` — \
      l'entrepôt ciblé doit être le sien.""")
  @ApiResponse(responseCode = "200", description = "Réservation automatique effectuée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "STOCK_RESERVE_FEFO",
        "message": "Réservation automatique (FEFO) effectuée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "entrepotId": "8a2c...",
          "medicamentId": "6b1f...",
          "quantiteDemandee": 300,
          "quantiteAllouee": 300,
          "entierementSatisfaite": true,
          "allocations": [
            { "lotId": "3e7a...", "numeroLot": "SAN-2026-0042", "quantiteAllouee": 200 },
            { "lotId": "9c4d...", "numeroLot": "SAN-2026-0051", "quantiteAllouee": 100 }
          ]
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou entrepôt ciblé différent du sien (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Entrepôt introuvable (ENTREPOT_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "Quantité demandée non entièrement satisfaisable par les lots disponibles — aucune réservation partielle conservée (STOCK_INSUFFICIENT)")
  @PostMapping("/reservations/fefo")
  ResponseEntity<Map<String, Object>> reserverFefo(@Valid @RequestBody ReserverStockFefoRequest request);

  @Operation(summary = "Libérer une réservation", description = """
      Libère tout ou partie d'une réservation existante (commande annulée, rejetée, ou \
      quantité revue à la baisse) — remet la quantité libérée à disposition de la vente.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA` — \
      l'entrepôt ciblé doit être le sien.""")
  @ApiResponse(responseCode = "200", description = "Réservation libérée")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou entrepôt ciblé différent du sien (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Ligne de stock introuvable (STOCK_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "Quantité réservée insuffisante pour la libération demandée (RESERVATION_INSUFFICIENT)")
  @PostMapping("/reservations/liberer")
  ResponseEntity<Map<String, Object>> libererReservation(@Valid @RequestBody LibererReservationRequest request);

  @Operation(summary = "Obtenir une ligne de stock", description = """
      Retourne le détail complet d'une ligne de stock (entrepôt, lot), y compris les \
      quantités disponible, réservée, disponible à la vente et en commande, ainsi que les \
      indicateurs `enRupture`/`seuilAtteint`. Un acteur PRA ne peut consulter qu'une ligne \
      de son propre entrepôt ; un acteur PNA a une portée illimitée.

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponse(responseCode = "200", description = "Stock récupéré")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant de consulter la ligne d'un autre entrepôt (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Ligne de stock introuvable (STOCK_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant de la ligne de stock") @PathVariable UUID id);

  @Operation(summary = "Définir le seuil d'alerte d'une ligne de stock", description = """
      Définit la quantité en-deçà de laquelle la ligne de stock est considérée comme \
      ayant atteint son seuil d'alerte (`seuilAtteint=true`, distinct de `enRupture`, qui \
      signifie une quantité disponible à la vente nulle). Un seuil `null` désactive \
      l'alerte pour cette ligne.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA` — \
      la ligne ciblée doit appartenir à son propre entrepôt.""")
  @ApiResponse(responseCode = "200", description = "Seuil d'alerte défini")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : seuil négatif")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou ligne appartenant à un autre entrepôt (ENTREPOT_SCOPE_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Ligne de stock introuvable (STOCK_NOT_FOUND)")
  @PatchMapping("/{id}/seuil-alerte")
  ResponseEntity<Map<String, Object>> definirSeuilAlerte(
      @Parameter(description = "Identifiant de la ligne de stock") @PathVariable UUID id,
      @Valid @RequestBody DefinirSeuilAlerteRequest request);

  @Operation(summary = "Lister les lignes de stock", description = """
      Recherche paginée sur l'ensemble des lignes de stock. `lotId`/`medicamentId` \
      filtrent sur une référence donnée. `ruptureUniquement=true` ne retourne que les \
      lignes en rupture (quantité disponible à la vente nulle). `seuilAtteintUniquement=true` \
      ne retourne que les lignes ayant atteint leur seuil d'alerte. `sortBy`/`sortDirection` \
      pilotent le tri (défaut `createdAt`/`DESC`).

      `entrepotId` n'est réellement utilisable que par un acteur PNA : pour un acteur PRA, \
      il est **ignoré** et systématiquement remplacé par son propre entrepôt.

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponse(responseCode = "200", description = "Liste des stocks récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "STOCKS_LISTED",
        "message": "Liste des stocks récupérée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "entrepotId": "8a2c...",
            "lotId": "3e7a...",
            "medicamentId": "6b1f...",
            "quantiteDisponible": 500,
            "quantiteReservee": 50,
            "quantiteDisponibleALaVente": 450,
            "quantiteEnCommande": 0,
            "seuilAlerte": 100,
            "enRupture": false,
            "seuilAtteint": false,
            "createdAt": "2024-01-01T12:00:00Z",
            "updatedAt": "2024-01-01T12:00:00Z"
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 15,
          "totalItems": 289,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Filtre sur un entrepôt — pris en compte uniquement pour un acteur PNA ; ignoré et forcé à son propre entrepôt pour un acteur PRA") @RequestParam(required = false) UUID entrepotId,
      @Parameter(description = "Filtre sur un lot") @RequestParam(required = false) UUID lotId,
      @Parameter(description = "Filtre sur un médicament") @RequestParam(required = false) UUID medicamentId,
      @Parameter(description = "Si true, ne retourne que les lignes en rupture") @RequestParam(required = false) Boolean ruptureUniquement,
      @Parameter(description = "Si true, ne retourne que les lignes ayant atteint leur seuil d'alerte") @RequestParam(required = false) Boolean seuilAtteintUniquement,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);

  @Operation(summary = "Lister les alertes de rupture", description = """
      Variante de la recherche de lignes de stock forcée sur `ruptureUniquement=true`, \
      triée par quantité disponible croissante (les plus critiques en premier). Cas \
      d'usage : tableau de bord d'alerte rupture.

      Mêmes règles de portée `entrepotId` que `GET /api/stocks` (ignoré et forcé pour un \
      acteur PRA).

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponse(responseCode = "200", description = "Alertes de rupture récupérées")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping("/alertes/rupture")
  ResponseEntity<Map<String, Object>> alertesRupture(
      @Parameter(description = "Filtre sur un entrepôt — pris en compte uniquement pour un acteur PNA ; ignoré et forcé à son propre entrepôt pour un acteur PRA") @RequestParam(required = false) UUID entrepotId,
      @Parameter(description = "Filtre sur un médicament") @RequestParam(required = false) UUID medicamentId,
      @Parameter(description = "Si true, ne retourne que les lignes ayant aussi atteint leur seuil d'alerte") @RequestParam(required = false) Boolean seuilAtteintUniquement,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);
}
