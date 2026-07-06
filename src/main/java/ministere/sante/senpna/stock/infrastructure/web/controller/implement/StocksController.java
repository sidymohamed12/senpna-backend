package ministere.sante.senpna.stock.infrastructure.web.controller.implement;

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
import ministere.sante.senpna.stock.infrastructure.web.controller.IStocksController;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class StocksController implements IStocksController {

        private static final String ROLES_MOUVEMENT = "hasAnyRole('ADMIN_PNA','MAGASINIER_PNA','ADMIN_PRA','MAGASINIER_PRA')";
        private static final String ROLES_RESERVATION = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')";
        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA')";

        private final StockFacade stockFacade;

        public StocksController(StockFacade stockFacade) {
                this.stockFacade = stockFacade;
        }

        @Override
        @PreAuthorize(ROLES_MOUVEMENT)
        public ResponseEntity<Map<String, Object>> entrer(EntreeStockRequest request) {
                StockDetail result = stockFacade.entrerStock(new EntreeStockCommand(
                                request.entrepotId(), request.lotId(), request.quantite(), request.typeMouvement(),
                                request.commandeId(), request.referenceDocument(), request.motif(), currentUserId()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result),
                                                "STOCK_ENTREE_ENREGISTREE",
                                                "Entrée en stock enregistrée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_MOUVEMENT)
        public ResponseEntity<Map<String, Object>> sortir(SortieStockRequest request) {
                StockDetail result = stockFacade.sortirStock(new SortieStockCommand(
                                request.entrepotId(), request.lotId(), request.quantite(), request.typeMouvement(),
                                request.depuisReservation(), request.entrepotDestinationId(), request.commandeId(),
                                request.referenceDocument(), request.motif(), currentUserId()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result),
                                                "STOCK_SORTIE_ENREGISTREE",
                                                "Sortie de stock enregistrée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_RESERVATION)
        public ResponseEntity<Map<String, Object>> reserver(ReserverStockRequest request) {
                StockDetail result = stockFacade.reserverStock(new ReserverStockCommand(
                                request.entrepotId(), request.lotId(), request.quantite(), request.commandeId()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_RESERVE",
                                "Quantité réservée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_RESERVATION)
        public ResponseEntity<Map<String, Object>> reserverFefo(ReserverStockFefoRequest request) {
                ReservationFefoResult result = stockFacade.reserverStockFefo(new ReserverStockFefoCommand(
                                request.entrepotId(), request.medicamentId(), request.quantiteDemandee(),
                                request.commandeId()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_RESERVE_FEFO",
                                "Réservation automatique (FEFO) effectuée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_RESERVATION)
        public ResponseEntity<Map<String, Object>> libererReservation(LibererReservationRequest request) {
                StockDetail result = stockFacade.libererReservation(new LibererReservationCommand(
                                request.entrepotId(), request.lotId(), request.quantite(), request.commandeId(),
                                request.motif()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "RESERVATION_LIBEREE",
                                "Réservation libérée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                StockDetail result = stockFacade.obtenirStock(new GetStockQuery(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_FOUND",
                                                "Stock récupéré"));
        }

        @Override
        @PreAuthorize(ROLES_RESERVATION)
        public ResponseEntity<Map<String, Object>> definirSeuilAlerte(UUID id, DefinirSeuilAlerteRequest request) {
                StockDetail result = stockFacade
                                .definirSeuilAlerte(new DefinirSeuilAlerteCommand(id, request.seuilAlerte()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "STOCK_SEUIL_DEFINI",
                                "Seuil d'alerte défini avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        UUID entrepotId, UUID lotId, UUID medicamentId, Boolean ruptureUniquement,
                        Boolean seuilAtteintUniquement, Integer page, Integer size, String sortBy,
                        String sortDirection) {

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

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> alertesRupture(
                        UUID entrepotId, UUID medicamentId, Boolean seuilAtteintUniquement, Integer page,
                        Integer size) {

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
                                detail.quantiteDisponible(), detail.quantiteReservee(),
                                detail.quantiteDisponibleALaVente(),
                                detail.quantiteEnCommande(), detail.seuilAlerte(), detail.enRupture(),
                                detail.seuilAtteint(),
                                detail.createdAt(), detail.updatedAt());
        }

        private ReservationFefoResponse toResponse(ReservationFefoResult result) {
                return new ReservationFefoResponse(result.entrepotId(), result.medicamentId(),
                                result.quantiteDemandee(),
                                result.quantiteAllouee(), result.estEntierementSatisfaite(),
                                result.allocations().stream().map(this::toResponse).toList());
        }

        private AllocationLotResponse toResponse(AllocationLot allocation) {
                return new AllocationLotResponse(allocation.lotId(), allocation.numeroLot(),
                                allocation.quantiteAllouee());
        }

        private UUID currentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CurrentUser principal = (CurrentUser) authentication.getPrincipal();
                return principal.getUserId();
        }
}
