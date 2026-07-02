package ministere.sante.senpna.organisation.infrastructure.web.controller;

import jakarta.validation.Valid;
import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetRegionQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreateRegionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.RegionResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Référentiel des régions administratives — donnée de base à laquelle sont
 * rattachées les PRA et les structures sanitaires.
 *
 * <pre>
 * POST /api/regions        {code, nom}
 * GET  /api/regions
 * GET  /api/regions/{id}
 * </pre>
 */
@RestController
@RequestMapping("/api/regions")
public class RegionsController {

    private final OrganisationFacade organisationFacade;

    public RegionsController(OrganisationFacade organisationFacade) {
        this.organisationFacade = organisationFacade;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateRegionRequest request) {
        RegionDetail result = organisationFacade.creerRegion(new CreateRegionCommand(request.code(), request.nom()));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "REGION_CREATED",
                        "Région créée avec succès"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> lister() {
        List<RegionResponse> results = organisationFacade.listerRegions().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, results, "REGIONS_LISTED",
                "Liste des régions récupérée"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
        RegionDetail result = organisationFacade.obtenirRegion(new GetRegionQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "REGION_FOUND",
                "Région récupérée"));
    }

    private RegionResponse toResponse(RegionDetail detail) {
        return new RegionResponse(detail.id(), detail.code(), detail.nom(), detail.actif(), detail.createdAt(),
                detail.updatedAt());
    }
}
