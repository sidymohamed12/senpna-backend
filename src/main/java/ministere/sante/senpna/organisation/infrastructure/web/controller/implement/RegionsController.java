package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import ministere.sante.senpna.organisation.application.facade.RegionFacade;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.GetRegionQuery;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;
import ministere.sante.senpna.organisation.infrastructure.web.controller.IRegionsController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreateRegionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.RegionResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class RegionsController implements IRegionsController {

    private final RegionFacade regionFacade;

    public RegionsController(RegionFacade regionFacade) {
        this.regionFacade = regionFacade;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> creer(CreateRegionRequest request) {
        RegionDetail result = regionFacade.creerRegion(new CreateRegionCommand(request.code(), request.nom()));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "REGION_CREATED",
                        "Région créée avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> lister() {
        List<RegionResponse> results = regionFacade.listerRegions().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, results, "REGIONS_LISTED",
                "Liste des régions récupérée"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        RegionDetail result = regionFacade.obtenirRegion(new GetRegionQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "REGION_FOUND",
                "Région récupérée"));
    }

    private RegionResponse toResponse(RegionDetail detail) {
        return new RegionResponse(detail.id(), detail.code(), detail.nom(), detail.actif(), detail.createdAt(),
                detail.updatedAt());
    }
}
