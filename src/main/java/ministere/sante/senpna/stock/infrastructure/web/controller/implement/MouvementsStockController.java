package ministere.sante.senpna.stock.infrastructure.web.controller.implement;

import ministere.sante.senpna.stock.application.facade.StockFacade;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.ListMouvementsQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;
import ministere.sante.senpna.stock.infrastructure.web.controller.IMouvementsStockController;
import ministere.sante.senpna.stock.infrastructure.web.dto.response.MouvementResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
public class MouvementsStockController implements IMouvementsStockController {

    private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA')";

    private final StockFacade stockFacade;

    public MouvementsStockController(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    @Override
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        MouvementDetail result = stockFacade.obtenirMouvement(new GetMouvementQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MOUVEMENT_FOUND",
                "Mouvement de stock récupéré"));
    }

    @Override
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> lister(
            UUID lotId, UUID medicamentId, UUID entrepotId, String typeMouvement, String sens,
            UUID utilisateurId, Instant dateDebut, Instant dateFin, Integer page, Integer size,
            String sortBy, String sortDirection) {

        MouvementPage result = stockFacade.listerMouvements(new ListMouvementsQuery(lotId, medicamentId, entrepotId,
                typeMouvement, sens, utilisateurId, dateDebut, dateFin, page, size, sortBy, sortDirection));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "MOUVEMENTS_LISTED",
                "Liste des mouvements de stock récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    private MouvementResponse toResponse(MouvementDetail detail) {
        return new MouvementResponse(detail.id(), detail.typeMouvement(), detail.sens(), detail.entrepotSourceId(),
                detail.entrepotDestinationId(), detail.commandeId(), detail.lotId(), detail.medicamentId(),
                detail.quantite(), detail.dateMouvement(), detail.referenceDocument(), detail.motif(),
                detail.utilisateurId(), detail.createdAt());
    }
}
