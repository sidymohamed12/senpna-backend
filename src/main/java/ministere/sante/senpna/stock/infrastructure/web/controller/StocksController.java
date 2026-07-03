package ministere.sante.senpna.stock.infrastructure.web.controller;

import jakarta.validation.Valid;

import ministere.sante.senpna.stock.application.facade.StockFacade;
import ministere.sante.senpna.stock.domain.command.StockCommands.AllocationLot;
import ministere.sante.senpna.stock.domain.command.StockCommands.DefinirSeuilAlerteCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.EntreeStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.GetStockQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.LibererReservationCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReservationFefoResult;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockFefoCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.SortieStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.DefinirSeuilAlerteRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.EntreeStockRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.LibererReservationRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ReserverStockFefoRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ReserverStockRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.SortieStockRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.response.AllocationLotResponse;
import ministere.sante.senpna.stock.infrastructure.web.dto.response.ReservationFefoResponse;
import ministere.sante.senpna.stock.infrastructure.web.dto.response.StockResponse;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Gestion du stock — entrées, sorties, réservations et consultation (cf.
 * doc. métier §9). Chaque opération mutative enregistre le
 * {@code MouvementStock} correspondant (cf. §13) dans la même transaction
 * applicative.
 *
 * <pre>
 * POST   /api/stocks/entrees                     {entrepotId, lotId, quantite, typeMouvement, commandeId?, referenceDocument?, motif?}
 * POST   /api/stocks/sorties                      {entrepotId, lotId, quantite, typeMouvement, depuisReservation, entrepotDestinationId?, commandeId?, referenceDocument?, motif?}
 * POST   /api/stocks/reservations                 {entrepotId, lotId, quantite, commandeId?}
 * POST   /api/stocks/reservations/fefo             {entrepotId, medicamentId, quantiteDemandee, commandeId?}
 * POST   /api/stocks/reservations/liberer          {entrepotId, lotId, quantite, commandeId?, motif?}
 * GET    /api/stocks/{id}
 * GET    /api/stocks?entrepotId=&lotId=&medicamentId=&ruptureUniquement=&seuilAtteintUniquement=&page=&size=&sortBy=&sortDirection=
 * GET    /api/stocks/alertes/rupture?entrepotId=&medicamentId=&seuilAtteintUniquement=&page=&size=
 * </pre>
 */
@RestController
@RequestMapping("/api/stocks")
public class StocksController {

    private static final String ROLES_MOUVEMENT = "hasAnyRole('ADMIN_PNA','MAGASINIER_PNA','ADMIN_PRA','MAGASINIER_PRA')";
    private static final String ROLES_RESERVATION = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')";
    private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA')";

    private final StockFacade stockFacade;

    public StocksController(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    @PostMapping("/entrees")
    @PreAuthorize(ROLES_MOUVEMENT)
    public ResponseEntity<Map<String, Object>> entrer(@Valid @RequestBody EntreeStockRequest request) {
        StockDetail result = stockFacade.entrerStock(new EntreeStockCommand(
                request.entrepotId(), request.lotId(), request.quantite(), request.typeMouvement(),
                request.commandeId(), request.referenceDocument(), request.motif(), currentUserId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "STOCK_ENTREE_ENREGISTREE",
                        "Entrée en stock enregistrée avec succès"));
    }

    @PostMapping("/sorties")
    @PreAuthorize(ROLES_MOUVEMENT)
    public ResponseEntity<Map<String, Object>> sortir(@Valid @RequestBody SortieStockRequest request) {
        StockDetail result = stockFacade.sortirStock(new SortieStockCommand(
                request.entrepotId(), request.lotId(), request.quantite(), request.typeMouvement(),
                request.depuisReservation(), request.entrepotDestinationId(), request.commandeId(),
                request.referenceDocument(), request.motif(), currentUserId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "STOCK_SORTIE_ENREGISTREE",
                        "Sortie de stock enregistrée avec succès"));
    }

    @PostMapping("/reservations")
    @PreAuthorize(ROLES_RESERVATION)
    public ResponseEntity<Map<String, Object>> reserver(@Valid @RequestBody ReserverStockRequest request) {
        StockDetail result = stockFacade.reserverStock(new ReserverStockCommand(
                request.entrepotId(), request.lotId(), request.quantite(), request.commandeId()));

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_RESERVE",
                "Quantité réservée avec succès"));
    }

    @PostMapping("/reservations/fefo")
    @PreAuthorize(ROLES_RESERVATION)
    public ResponseEntity<Map<String, Object>> reserverFefo(@Valid @RequestBody ReserverStockFefoRequest request) {
        ReservationFefoResult result = stockFacade.reserverStockFefo(new ReserverStockFefoCommand(
                request.entrepotId(), request.medicamentId(), request.quantiteDemandee(), request.commandeId()));

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_RESERVE_FEFO",
                "Réservation automatique (FEFO) effectuée avec succès"));
    }

    @PostMapping("/reservations/liberer")
    @PreAuthorize(ROLES_RESERVATION)
    public ResponseEntity<Map<String, Object>> libererReservation(
            @Valid @RequestBody LibererReservationRequest request) {
        StockDetail result = stockFacade.libererReservation(new LibererReservationCommand(
                request.entrepotId(), request.lotId(), request.quantite(), request.commandeId(), request.motif()));

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "RESERVATION_LIBEREE",
                "Réservation libérée avec succès"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
        StockDetail result = stockFacade.obtenirStock(new GetStockQuery(id));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_FOUND", "Stock récupéré"));
    }

    @PatchMapping("/{id}/seuil-alerte")
    @PreAuthorize(ROLES_RESERVATION)
    public ResponseEntity<Map<String, Object>> definirSeuilAlerte(@PathVariable UUID id,
            @Valid @RequestBody DefinirSeuilAlerteRequest request) {
        StockDetail result = stockFacade
                .definirSeuilAlerte(new DefinirSeuilAlerteCommand(id, request.seuilAlerte()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_SEUIL_DEFINI",
                "Seuil d'alerte défini avec succès"));
    }

    @GetMapping
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> lister(
            @RequestParam(required = false) UUID entrepotId,
            @RequestParam(required = false) UUID lotId,
            @RequestParam(required = false) UUID medicamentId,
            @RequestParam(required = false) Boolean ruptureUniquement,
            @RequestParam(required = false) Boolean seuilAtteintUniquement,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

        StockPage result = stockFacade.listerStocks(new ListStocksQuery(entrepotId, lotId, medicamentId,
                ruptureUniquement, seuilAtteintUniquement, page, size, sortBy, sortDirection));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "STOCKS_LISTED",
                "Liste des stocks récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @GetMapping("/alertes/rupture")
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> alertesRupture(
            @RequestParam(required = false) UUID entrepotId,
            @RequestParam(required = false) UUID medicamentId,
            @RequestParam(required = false) Boolean seuilAtteintUniquement,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        StockPage result = stockFacade.listerAlertesRupture(new ListStocksQuery(entrepotId, null, medicamentId,
                Boolean.TRUE, seuilAtteintUniquement, page, size, "quantiteDisponible", "ASC"));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "STOCK_ALERTES_RUPTURE_LISTED",
                "Alertes de rupture récupérées",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    private StockResponse toResponse(StockDetail detail) {
        return new StockResponse(detail.id(), detail.entrepotId(), detail.lotId(), detail.medicamentId(),
                detail.quantiteDisponible(), detail.quantiteReservee(), detail.quantiteDisponibleALaVente(),
                detail.quantiteEnCommande(), detail.seuilAlerte(), detail.enRupture(), detail.seuilAtteint(),
                detail.createdAt(), detail.updatedAt());
    }

    private ReservationFefoResponse toResponse(ReservationFefoResult result) {
        return new ReservationFefoResponse(result.entrepotId(), result.medicamentId(), result.quantiteDemandee(),
                result.quantiteAllouee(), result.estEntierementSatisfaite(),
                result.allocations().stream().map(this::toResponse).toList());
    }

    private AllocationLotResponse toResponse(AllocationLot allocation) {
        return new AllocationLotResponse(allocation.lotId(), allocation.numeroLot(), allocation.quantiteAllouee());
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser principal = (CurrentUser) authentication.getPrincipal();
        return principal.getUserId();
    }
}
