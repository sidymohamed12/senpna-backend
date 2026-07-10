package ministere.sante.senpna.stock.infrastructure.web.controller.implement;

import ministere.sante.senpna.stock.application.facade.LotFacade;
import ministere.sante.senpna.stock.domain.command.LotCommands.AlertePeremptionQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.BloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.CreerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.DebloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.GetLotQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.ListLotsQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.domain.command.LotCommands.ModifierPrixLotCommand;
import ministere.sante.senpna.stock.infrastructure.web.controller.ILotsController;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.CreerLotRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ModifierPrixLotRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.response.LotResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class LotsController implements ILotsController {

        // La création d'un lot est réservée aux rôles PNA (cf. modèle métier
        // complémentaire §2 « Approvisionnements » : « Les achats fournisseurs
        // sont effectués uniquement par la PNA ») — une PRA ne crée jamais de
        // lot, elle en reçoit par transfert. Doublé d'une vérification en
        // couche application (EntrepotScopeGuard.verifierActeurNational())
        // pour ne pas dépendre uniquement de cette annotation.
        private static final String ROLES_CREATION = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')";
        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA')";
        private static final String ROLES_PHARMACOVIGILANCE = "hasAnyRole('ADMIN_PNA','PHARMACIEN_PNA','ADMIN_PRA','PHARMACIEN_PRA')";
        private static final String ROLES_PRIX = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')";

        private final LotFacade stockFacade;

        public LotsController(LotFacade stockFacade) {
                this.stockFacade = stockFacade;
        }

        @Override
        @PreAuthorize(ROLES_CREATION)
        public ResponseEntity<Map<String, Object>> creer(CreerLotRequest request) {
                LotDetail result = stockFacade.creerLot(new CreerLotCommand(
                                request.numeroLot(), request.medicamentId(), request.fournisseurId(),
                                request.dateFabrication(),
                                request.dateExpiration(), request.prixAchat(), request.prixVente()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "LOT_CREATED",
                                                "Lot créé avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_PHARMACOVIGILANCE)
        public ResponseEntity<Map<String, Object>> bloquer(UUID id) {
                LotDetail result = stockFacade.bloquerLot(new BloquerLotCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_BLOCKED",
                                "Lot bloqué avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_PHARMACOVIGILANCE)
        public ResponseEntity<Map<String, Object>> debloquer(UUID id) {
                LotDetail result = stockFacade.debloquerLot(new DebloquerLotCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_UNBLOCKED",
                                "Lot débloqué avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_PRIX)
        public ResponseEntity<Map<String, Object>> modifierPrix(UUID id, ModifierPrixLotRequest request) {
                LotDetail result = stockFacade
                                .modifierPrixLot(new ModifierPrixLotCommand(id, request.prixAchat(),
                                                request.prixVente()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_PRICE_UPDATED",
                                "Prix du lot modifié avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                LotDetail result = stockFacade.obtenirLot(new GetLotQuery(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_FOUND",
                                                "Lot récupéré"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        String q, UUID medicamentId, UUID fournisseurId, String statut, UUID entrepotId,
                        Integer page, Integer size, String sortBy, String sortDirection) {

                // entrepotId : ignoré et forcé au sien pour un acteur PRA, libre pour
                // un acteur PNA (cf. EntrepotScopeGuard.entrepotIdPourLecture()) —
                // c'est ainsi qu'un acteur PNA peut lister/filtrer sur n'importe
                // quelle PRA en lecture.
                LotPage result = stockFacade.listerLots(
                                new ListLotsQuery(q, medicamentId, fournisseurId, statut, entrepotId, page, size,
                                                sortBy,
                                                sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "LOTS_LISTED",
                                "Liste des lots récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> alertesPeremption(
                        int horizonJours, UUID medicamentId, UUID entrepotId, Integer page, Integer size) {

                LotPage result = stockFacade.listerAlertesPeremption(
                                new AlertePeremptionQuery(horizonJours, medicamentId, entrepotId, page, size));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "LOT_ALERTES_PEREMPTION_LISTED",
                                "Alertes de péremption récupérées",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private LotResponse toResponse(LotDetail detail) {
                return new LotResponse(detail.id(), detail.numeroLot(), detail.medicamentId(), detail.fournisseurId(),
                                detail.dateFabrication(), detail.dateExpiration(), detail.prixAchat(),
                                detail.prixVente(),
                                detail.statut(), detail.expire(), detail.joursAvantExpiration(), detail.createdAt(),
                                detail.updatedAt());
        }
}
