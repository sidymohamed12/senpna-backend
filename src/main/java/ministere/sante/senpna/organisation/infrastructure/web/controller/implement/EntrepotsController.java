package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetEntrepotQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListEntrepotsQuery;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.web.controller.IEntrepotsController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.EntrepotResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
public class EntrepotsController implements IEntrepotsController {

    private final OrganisationFacade organisationFacade;

    public EntrepotsController(OrganisationFacade organisationFacade) {
        this.organisationFacade = organisationFacade;
    }

    @Override
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        EntrepotDetail result = organisationFacade.obtenirEntrepot(new GetEntrepotQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ENTREPOT_FOUND",
                "Entrepôt récupéré"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> lister(
            String q, TypeEntrepot type, UUID regionId, Boolean actif, Integer page, Integer size,
            String sortBy, String sortDirection) {

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
