package ministere.sante.senpna.stock.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.stock.infrastructure.web.controller.implement.LotsController;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.CreerLotRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ModifierPrixLotRequest;

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
 * Contrat API du contrôleur Lots.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link LotsController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link LotsController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST   /api/lots                        {numeroLot, medicamentId, fournisseurId, dateFabrication?, dateExpiration, prixAchat?, prixVente?}
 * PATCH  /api/lots/{id}/bloquer
 * PATCH  /api/lots/{id}/debloquer
 * PATCH  /api/lots/{id}/prix               {prixAchat?, prixVente?}
 * GET    /api/lots/{id}
 * GET    /api/lots?q=&medicamentId=&fournisseurId=&statut=&entrepotId=&page=&size=&sortBy=&sortDirection=
 * GET    /api/lots/alertes/peremption?horizonJours=&medicamentId=&entrepotId=&page=&size=
 * </pre>
 */
@Tag(name = "Lots", description = """
    Gestion des lots de médicaments — référentiel partagé, indépendant de leur présence \
    physique dans un entrepôt donné. La mise en stock effective d'un lot (entrée en stock) est \
    gérée séparément par le contrôleur Stocks.""")
@RequestMapping("/api/lots")
public interface ILotsController {

  @Operation(summary = "Créer un lot", description = """
      Enregistre un nouveau lot dans le référentiel, à l'état `ACTIF`. N'affecte aucune \
      quantité de stock — la mise en stock effective se fait ensuite via \
      `POST /api/stocks/entrees`, dans un souci de responsabilité unique par opération.

      Seule la PNA achète auprès des fournisseurs : une PRA ne crée jamais de lot, elle en \
      reçoit par transfert. `numeroLot` doit être unique par médicament (deux médicaments \
      différents peuvent partager le même numéro de lot fournisseur).

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `PHARMACIEN_PNA` ou `MAGASINIER_PNA`.""")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Lot créé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "status": 201,
            "type": "LOT_CREATED",
            "message": "Lot créé avec succès",
            "timestamp": "2024-01-01T12:00:00Z",
            "results": {
              "id": "550e8400-e29b-41d4-a716-446655440000",
              "numeroLot": "SAN-2026-0042",
              "medicamentId": "8a2c...",
              "fournisseurId": "3e7a...",
              "dateFabrication": "2025-11-01",
              "dateExpiration": "2027-11-01",
              "prixAchat": 500,
              "prixVente": 750,
              "statut": "ACTIF",
              "expire": false,
              "joursAvantExpiration": 700,
              "createdAt": "2024-01-01T12:00:00Z",
              "updatedAt": "2024-01-01T12:00:00Z"
            }
          }
          """))),
      @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : numéro de lot/médicament/fournisseur/date d'expiration manquant, prix négatif"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer un lot (réservé aux acteurs PNA)"),
      @ApiResponse(responseCode = "404", description = "Médicament ou fournisseur introuvable (MEDICAMENT_NOT_FOUND / FOURNISSEUR_NOT_FOUND)"),
      @ApiResponse(responseCode = "409", description = "Un lot avec ce numéro existe déjà pour ce médicament (LOT_NUMERO_ALREADY_USED)")
  })
  @PostMapping
  ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreerLotRequest request);

  @Operation(summary = "Bloquer un lot (mise en quarantaine)", description = """
      Bloque le lot — typiquement déclenché suite à une déclaration de \
      pharmacovigilance. Un lot bloqué ne peut plus être réservé ni expédié tant qu'il \
      n'est pas explicitement débloqué. Un acteur PRA ne peut agir que sur un lot déjà \
      présent dans le stock de son propre entrepôt ; un acteur PNA a une portée illimitée. \
      Opération idempotente sur un lot déjà bloqué.

      Rôle requis : `ADMIN_PNA`, `PHARMACIEN_PNA`, `ADMIN_PRA` ou `PHARMACIEN_PRA`.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lot bloqué"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant d'agir sur un lot jamais présent dans son entrepôt (LOT_OUT_OF_SCOPE)"),
      @ApiResponse(responseCode = "404", description = "Lot introuvable (LOT_NOT_FOUND)"),
      @ApiResponse(responseCode = "422", description = "Le lot est expiré — statut terminal, ne peut plus être bloqué (LOT_EXPIRED)")
  })
  @PatchMapping("/{id}/bloquer")
  ResponseEntity<Map<String, Object>> bloquer(
      @Parameter(description = "Identifiant du lot") @PathVariable UUID id);

  @Operation(summary = "Débloquer un lot", description = """
      Ramène le lot à l'état `ACTIF`, le rendant à nouveau réservable et expédiable. \
      Mêmes règles de portée qu'au blocage. Opération idempotente sur un lot déjà actif.

      Rôle requis : `ADMIN_PNA`, `PHARMACIEN_PNA`, `ADMIN_PRA` ou `PHARMACIEN_PRA`.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lot débloqué"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant d'agir sur un lot jamais présent dans son entrepôt (LOT_OUT_OF_SCOPE)"),
      @ApiResponse(responseCode = "404", description = "Lot introuvable (LOT_NOT_FOUND)"),
      @ApiResponse(responseCode = "422", description = "Le lot est expiré — statut terminal, ne peut plus être débloqué (LOT_EXPIRED)")
  })
  @PatchMapping("/{id}/debloquer")
  ResponseEntity<Map<String, Object>> debloquer(
      @Parameter(description = "Identifiant du lot") @PathVariable UUID id);

  @Operation(summary = "Modifier le prix d'un lot", description = """
      Modifie le prix d'achat et/ou de vente d'un lot déjà créé — utile lorsqu'un prix a \
      été saisi provisoirement ou doit être corrigé après réception. Mêmes règles de \
      portée qu'au blocage (un acteur PRA ne peut agir que sur un lot présent dans son \
      entrepôt).

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA` uniquement — contrairement au \
      blocage/déblocage, la modification du prix n'est pas ouverte aux rôles PRA.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Prix du lot modifié"),
      @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : prix négatif"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant d'agir sur un lot jamais présent dans son entrepôt (LOT_OUT_OF_SCOPE)"),
      @ApiResponse(responseCode = "404", description = "Lot introuvable (LOT_NOT_FOUND)")
  })
  @PatchMapping("/{id}/prix")
  ResponseEntity<Map<String, Object>> modifierPrix(
      @Parameter(description = "Identifiant du lot") @PathVariable UUID id,
      @Valid @RequestBody ModifierPrixLotRequest request);

  @Operation(summary = "Obtenir un lot", description = """
      Retourne le détail complet d'un lot, y compris son statut (`ACTIF`, `BLOQUE`, \
      `EXPIRE`) et le nombre de jours avant expiration. Un acteur PRA ne peut consulter \
      qu'un lot déjà présent dans le stock de son entrepôt ; un acteur PNA a une portée \
      illimitée sur le référentiel des lots.

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lot récupéré"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant de consulter un lot jamais présent dans son entrepôt (LOT_OUT_OF_SCOPE)"),
      @ApiResponse(responseCode = "404", description = "Lot introuvable (LOT_NOT_FOUND)")
  })
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant du lot") @PathVariable UUID id);

  @Operation(summary = "Lister les lots", description = """
      Recherche paginée sur l'ensemble des lots. `q` recherche en texte libre (numéro de \
      lot). `medicamentId`/`fournisseurId` filtrent sur une référence donnée. `statut` \
      filtre sur `ACTIF`, `BLOQUE` ou `EXPIRE`. `sortBy`/`sortDirection` pilotent le tri \
      (défaut `dateExpiration`/`ASC`, pour prioriser naturellement les lots les plus \
      proches de leur péremption — cf. règle FEFO).

      `entrepotId` n'est réellement utilisable que par un acteur PNA (portée nationale) : \
      pour un acteur PRA, il est **ignoré** et systématiquement remplacé par son propre \
      entrepôt — un acteur PRA ne voit donc jamais que les lots déjà présents dans son \
      propre stock.

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Liste des lots récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "status": 200,
            "type": "LOTS_LISTED",
            "message": "Liste des lots récupérée",
            "timestamp": "2024-01-01T12:00:00Z",
            "results": [
              {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "numeroLot": "SAN-2026-0042",
                "medicamentId": "8a2c...",
                "fournisseurId": "3e7a...",
                "dateFabrication": "2025-11-01",
                "dateExpiration": "2027-11-01",
                "prixAchat": 500,
                "prixVente": 750,
                "statut": "ACTIF",
                "expire": false,
                "joursAvantExpiration": 700,
                "createdAt": "2024-01-01T12:00:00Z",
                "updatedAt": "2024-01-01T12:00:00Z"
              }
            ],
            "pagination": {
              "currentPage": 0,
              "totalPages": 8,
              "totalItems": 152,
              "first": true,
              "last": false
            }
          }
          """))),
      @ApiResponse(responseCode = "400", description = "Statut de lot invalide (LOT_STATUT_INVALID)"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  })
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Recherche texte libre sur le numéro de lot") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur un médicament") @RequestParam(required = false) UUID medicamentId,
      @Parameter(description = "Filtre sur un fournisseur") @RequestParam(required = false) UUID fournisseurId,
      @Parameter(description = "Filtre sur le statut (ACTIF, BLOQUE, EXPIRE)") @RequestParam(required = false) String statut,
      @Parameter(description = "Filtre sur un entrepôt — pris en compte uniquement pour un acteur PNA ; ignoré et forcé à son propre entrepôt pour un acteur PRA") @RequestParam(required = false) UUID entrepotId,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "dateExpiration") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "ASC") String sortDirection);

  @Operation(summary = "Lister les alertes de péremption", description = """
      Variante de la recherche de lots restreinte à ceux dont la date d'expiration tombe \
      dans l'horizon donné (`horizonJours`, en jours à partir d'aujourd'hui — doit être \
      strictement positif), triée par date d'expiration croissante. Cas d'usage : tableau \
      de bord d'alerte péremption pour anticiper les pertes.

      Mêmes règles de portée `entrepotId` que `GET /api/lots` (ignoré et forcé pour un \
      acteur PRA).

      Rôle requis : acteurs PNA ou PRA (tous rôles).""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Alertes de péremption récupérées"),
      @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
      @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  })
  @GetMapping("/alertes/peremption")
  ResponseEntity<Map<String, Object>> alertesPeremption(
      @Parameter(description = "Horizon en jours à partir d'aujourd'hui (doit être strictement positif)") @RequestParam(required = false, defaultValue = "365") int horizonJours,
      @Parameter(description = "Filtre sur un médicament") @RequestParam(required = false) UUID medicamentId,
      @Parameter(description = "Filtre sur un entrepôt — pris en compte uniquement pour un acteur PNA ; ignoré et forcé à son propre entrepôt pour un acteur PRA") @RequestParam(required = false) UUID entrepotId,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);
}
