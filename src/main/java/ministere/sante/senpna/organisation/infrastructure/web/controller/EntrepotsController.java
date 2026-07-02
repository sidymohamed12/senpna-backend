package ministere.sante.senpna.organisation.infrastructure.web.controller;

import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetEntrepotQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListEntrepotsQuery;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.EntrepotResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Consultation générique des entrepôts, tous types confondus (PRA et
 * PNA centrale) — utilisé notamment pour peupler le choix d'entrepôt
 * lors de la création d'un compte utilisateur par un acteur national
 * (cf. {@code EntrepotAffectationResolver}). Pour la gestion complète du
 * cycle de vie d'une PRA (création/modification/désactivation), voir
 * {@link PrasController}.
 *
 * <pre>
 * GET /api/entrepots/{id}
 * GET /api/entrepots?q=&type=&regionId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/entrepots")
@PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
public class EntrepotsController {

    private final OrganisationFacade organisationFacade;

    public EntrepotsController(OrganisationFacade organisationFacade) {
        this.organisationFacade = organisationFacade;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
        EntrepotDetail result = organisationFacade.obtenirEntrepot(new GetEntrepotQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ENTREPOT_FOUND",
                "Entrepôt récupéré"));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> lister(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TypeEntrepot type,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

        EntrepotPage result = organisationFacade.listerEntrepots(
                new ListEntrepotsQuery(q, type, regionId, actif, page, size, sortBy, sortDirection));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "ENTREPOTS_LISTED",
                "Liste des entrepôts récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    private EntrepotResponse toResponse(EntrepotDetail detail) {
        return new EntrepotResponse(detail.id(), detail.code(), detail.nom(), detail.type(), detail.regionId(),
                detail.regionNom(), detail.adresse(), detail.telephone(), detail.responsableUserId(),
                detail.actif(), detail.createdAt(), detail.updatedAt());
    }
}
