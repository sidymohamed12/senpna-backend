package ministere.sante.senpna.organisation.infrastructure.web.controller;

import jakarta.validation.Valid;
import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ActivateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignStructureToPraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignStructureToRegionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.DeactivateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetStructureSanitaireQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListStructuresSanitairesQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RejectAdhesionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitairePage;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UpdateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ValidateAdhesionCommand;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignPraRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignRegionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreateStructureSanitaireRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.RejectAdhesionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.UpdateStructureSanitaireRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.StructureSanitaireResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Gestion des structures sanitaires bénéficiaires (hôpitaux, districts,
 * centres et postes de santé, ONG) — cf. doc. métier §3.
 *
 * <pre>
 * POST   /api/structures-sanitaires                      {code, nom, type, district?, adresse?, telephone?, email?, responsable?}
 * PUT    /api/structures-sanitaires/{id}                  {nom, district?, adresse?, telephone?, email?, responsable?}
 * PATCH  /api/structures-sanitaires/{id}/valider-adhesion
 * PATCH  /api/structures-sanitaires/{id}/rejeter-adhesion {motif}
 * PATCH  /api/structures-sanitaires/{id}/activer
 * PATCH  /api/structures-sanitaires/{id}/desactiver
 * PATCH  /api/structures-sanitaires/{id}/region           {regionId}
 * PATCH  /api/structures-sanitaires/{id}/pra              {praId}
 * GET    /api/structures-sanitaires/{id}
 * GET    /api/structures-sanitaires?q=&type=&regionId=&praId=&statutAdhesion=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/structures-sanitaires")
public class StructuresSanitairesController {

    private final OrganisationFacade organisationFacade;

    public StructuresSanitairesController(OrganisationFacade organisationFacade) {
        this.organisationFacade = organisationFacade;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateStructureSanitaireRequest request) {
        StructureSanitaireDetail result = organisationFacade.creerStructureSanitaire(
                new CreateStructureSanitaireCommand(request.code(), request.nom(), request.type(),
                        request.district(), request.adresse(), request.telephone(), request.email(),
                        request.responsable()));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "STRUCTURE_SANITAIRE_CREATED",
                        "Demande d'adhésion de la structure sanitaire enregistrée"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
            @Valid @RequestBody UpdateStructureSanitaireRequest request) {
        StructureSanitaireDetail result = organisationFacade.modifierStructureSanitaire(
                new UpdateStructureSanitaireCommand(id, request.nom(), request.district(), request.adresse(),
                        request.telephone(), request.email(), request.responsable()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "STRUCTURE_SANITAIRE_UPDATED", "Structure sanitaire modifiée avec succès"));
    }

    @PatchMapping("/{id}/valider-adhesion")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> validerAdhesion(@PathVariable UUID id) {
        StructureSanitaireDetail result = organisationFacade.validerAdhesion(new ValidateAdhesionCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ADHESION_VALIDATED",
                "Demande d'adhésion validée avec succès"));
    }

    @PatchMapping("/{id}/rejeter-adhesion")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> rejeterAdhesion(@PathVariable UUID id,
            @Valid @RequestBody RejectAdhesionRequest request) {
        StructureSanitaireDetail result = organisationFacade
                .rejeterAdhesion(new RejectAdhesionCommand(id, request.motif()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ADHESION_REJECTED",
                "Demande d'adhésion rejetée"));
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> activer(@PathVariable UUID id) {
        StructureSanitaireDetail result = organisationFacade
                .activerStructureSanitaire(new ActivateStructureSanitaireCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "STRUCTURE_SANITAIRE_ACTIVATED", "Structure sanitaire activée avec succès"));
    }

    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> desactiver(@PathVariable UUID id) {
        StructureSanitaireDetail result = organisationFacade
                .desactiverStructureSanitaire(new DeactivateStructureSanitaireCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "STRUCTURE_SANITAIRE_DEACTIVATED", "Structure sanitaire désactivée avec succès"));
    }

    @PatchMapping("/{id}/region")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> affecterRegion(@PathVariable UUID id,
            @Valid @RequestBody AssignRegionRequest request) {
        StructureSanitaireDetail result = organisationFacade
                .affecterStructureARegion(new AssignStructureToRegionCommand(id, request.regionId()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "STRUCTURE_SANITAIRE_REGION_ASSIGNED", "Structure sanitaire rattachée à la région avec succès"));
    }

    @PatchMapping("/{id}/pra")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> affecterPra(@PathVariable UUID id,
            @Valid @RequestBody AssignPraRequest request) {
        StructureSanitaireDetail result = organisationFacade
                .affecterStructureAPra(new AssignStructureToPraCommand(id, request.praId()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "STRUCTURE_SANITAIRE_PRA_ASSIGNED", "Structure sanitaire rattachée à la PRA avec succès"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
        StructureSanitaireDetail result = organisationFacade
                .obtenirStructureSanitaire(new GetStructureSanitaireQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "STRUCTURE_SANITAIRE_FOUND", "Structure sanitaire récupérée"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
    public ResponseEntity<Map<String, Object>> lister(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TypeStructureSanitaire type,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) UUID praId,
            @RequestParam(required = false) StatutAdhesion statutAdhesion,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

        StructureSanitairePage result = organisationFacade.listerStructuresSanitaires(
                new ListStructuresSanitairesQuery(q, type, regionId, praId, statutAdhesion, actif, page, size,
                        sortBy, sortDirection));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "STRUCTURES_SANITAIRES_LISTED",
                "Liste des structures sanitaires récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    private StructureSanitaireResponse toResponse(StructureSanitaireDetail detail) {
        return new StructureSanitaireResponse(detail.id(), detail.code(), detail.nom(), detail.type(),
                detail.regionId(), detail.regionNom(), detail.praId(), detail.praNom(), detail.district(),
                detail.adresse(), detail.telephone(), detail.email(), detail.responsable(),
                detail.statutAdhesion(), detail.motifRejet(), detail.actif(), detail.createdAt(),
                detail.updatedAt());
    }
}
