package ministere.sante.senpna.stock.infrastructure.web.controller;

import ministere.sante.senpna.stock.application.facade.StockFacade;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.ListMouvementsQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;
import ministere.sante.senpna.stock.infrastructure.web.dto.response.MouvementResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Consultation du journal des mouvements de stock (cf. doc. métier §13 et
 * §18) — historique, filtrage et traçabilité complète. Lecture seule :
 * les mouvements sont créés exclusivement en conséquence d'une entrée ou
 * d'une sortie de stock (cf. {@link StocksController}), jamais
 * directement.
 *
 * <pre>
 * GET /api/mouvements-stock/{id}
 * GET /api/mouvements-stock?lotId=&medicamentId=&entrepotId=&typeMouvement=&sens=&utilisateurId=&dateDebut=&dateFin=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/mouvements-stock")
public class MouvementsStockController {

    private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA')";

    private final StockFacade stockFacade;

    public MouvementsStockController(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    @GetMapping("/{id}")
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
        MouvementDetail result = stockFacade.obtenirMouvement(new GetMouvementQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MOUVEMENT_FOUND",
                "Mouvement de stock récupéré"));
    }

    @GetMapping
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> lister(
            @RequestParam(required = false) UUID lotId,
            @RequestParam(required = false) UUID medicamentId,
            @RequestParam(required = false) UUID entrepotId,
            @RequestParam(required = false) String typeMouvement,
            @RequestParam(required = false) String sens,
            @RequestParam(required = false) UUID utilisateurId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFin,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "dateMouvement") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

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
