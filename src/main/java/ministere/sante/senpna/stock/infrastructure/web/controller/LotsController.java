package ministere.sante.senpna.stock.infrastructure.web.controller;

import jakarta.validation.Valid;

import ministere.sante.senpna.stock.application.facade.StockFacade;
import ministere.sante.senpna.stock.domain.command.LotCommands.AlertePeremptionQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.BloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.CreerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.DebloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.GetLotQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.ListLotsQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.domain.command.LotCommands.ModifierPrixLotCommand;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.CreerLotRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ModifierPrixLotRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.response.LotResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Gestion des lots de médicaments (cf. doc. métier §8 et modèle métier
 * complémentaire §2 « Lots »). La mise en stock effective d'un lot
 * (entrée en stock) est gérée séparément par {@link StocksController}.
 *
 * <pre>
 * POST   /api/lots                        {numeroLot, medicamentId, fournisseurId, dateFabrication?, dateExpiration, prixAchat?, prixVente?}
 * PATCH  /api/lots/{id}/bloquer
 * PATCH  /api/lots/{id}/debloquer
 * PATCH  /api/lots/{id}/prix               {prixAchat?, prixVente?}
 * GET    /api/lots/{id}
 * GET    /api/lots?q=&medicamentId=&fournisseurId=&statut=&page=&size=&sortBy=&sortDirection=
 * GET    /api/lots/alertes/peremption?horizonJours=&medicamentId=&page=&size=
 * </pre>
 */
@RestController
@RequestMapping("/api/lots")
public class LotsController {

    private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA')";
    private static final String ROLES_LECTURE = ROLES_ECRITURE;
    private static final String ROLES_PHARMACOVIGILANCE = "hasAnyRole('ADMIN_PNA','PHARMACIEN_PNA','ADMIN_PRA','PHARMACIEN_PRA')";
    private static final String ROLES_PRIX = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')";

    private final StockFacade stockFacade;

    public LotsController(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    @PostMapping
    @PreAuthorize(ROLES_ECRITURE)
    public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreerLotRequest request) {
        LotDetail result = stockFacade.creerLot(new CreerLotCommand(
                request.numeroLot(), request.medicamentId(), request.fournisseurId(), request.dateFabrication(),
                request.dateExpiration(), request.prixAchat(), request.prixVente()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "LOT_CREATED",
                        "Lot créé avec succès"));
    }

    @PatchMapping("/{id}/bloquer")
    @PreAuthorize(ROLES_PHARMACOVIGILANCE)
    public ResponseEntity<Map<String, Object>> bloquer(@PathVariable UUID id) {
        LotDetail result = stockFacade.bloquerLot(new BloquerLotCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_BLOCKED",
                "Lot bloqué avec succès"));
    }

    @PatchMapping("/{id}/debloquer")
    @PreAuthorize(ROLES_PHARMACOVIGILANCE)
    public ResponseEntity<Map<String, Object>> debloquer(@PathVariable UUID id) {
        LotDetail result = stockFacade.debloquerLot(new DebloquerLotCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_UNBLOCKED",
                "Lot débloqué avec succès"));
    }

    @PatchMapping("/{id}/prix")
    @PreAuthorize(ROLES_PRIX)
    public ResponseEntity<Map<String, Object>> modifierPrix(@PathVariable UUID id,
            @Valid @RequestBody ModifierPrixLotRequest request) {
        LotDetail result = stockFacade
                .modifierPrixLot(new ModifierPrixLotCommand(id, request.prixAchat(), request.prixVente()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_PRICE_UPDATED",
                "Prix du lot modifié avec succès"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
        LotDetail result = stockFacade.obtenirLot(new GetLotQuery(id));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "LOT_FOUND", "Lot récupéré"));
    }

    @GetMapping
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> lister(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID medicamentId,
            @RequestParam(required = false) UUID fournisseurId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "dateExpiration") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {

        LotPage result = stockFacade.listerLots(
                new ListLotsQuery(q, medicamentId, fournisseurId, statut, page, size, sortBy, sortDirection));

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

    @GetMapping("/alertes/peremption")
    @PreAuthorize(ROLES_LECTURE)
    public ResponseEntity<Map<String, Object>> alertesPeremption(
            @RequestParam(required = false, defaultValue = "365") int horizonJours,
            @RequestParam(required = false) UUID medicamentId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        LotPage result = stockFacade
                .listerAlertesPeremption(new AlertePeremptionQuery(horizonJours, medicamentId, page, size));

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
                detail.dateFabrication(), detail.dateExpiration(), detail.prixAchat(), detail.prixVente(),
                detail.statut(), detail.expire(), detail.joursAvantExpiration(), detail.createdAt(),
                detail.updatedAt());
    }
}
