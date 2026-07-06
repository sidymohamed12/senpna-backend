package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

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
import ministere.sante.senpna.organisation.infrastructure.web.controller.IStructuresSanitairesController;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class StructuresSanitairesController implements IStructuresSanitairesController {

        private final OrganisationFacade organisationFacade;

        public StructuresSanitairesController(OrganisationFacade organisationFacade) {
                this.organisationFacade = organisationFacade;
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> creer(CreateStructureSanitaireRequest request) {
                StructureSanitaireDetail result = organisationFacade.creerStructureSanitaire(
                                new CreateStructureSanitaireCommand(request.code(), request.nom(), request.type(),
                                                request.regionId(), request.district(), request.adresse(),
                                                request.telephone(),
                                                request.email(), request.responsableNom(),
                                                request.responsablePrenom()));
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result),
                                                "STRUCTURE_SANITAIRE_CREATED",
                                                "Demande d'adhésion de la structure sanitaire enregistrée"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateStructureSanitaireRequest request) {
                StructureSanitaireDetail result = organisationFacade.modifierStructureSanitaire(
                                new UpdateStructureSanitaireCommand(id, request.nom(), request.district(),
                                                request.adresse(),
                                                request.telephone(), request.email(), request.responsableNom(),
                                                request.responsablePrenom()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "STRUCTURE_SANITAIRE_UPDATED", "Structure sanitaire modifiée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> validerAdhesion(UUID id) {
                StructureSanitaireDetail result = organisationFacade.validerAdhesion(new ValidateAdhesionCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ADHESION_VALIDATED",
                                "Demande d'adhésion validée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> rejeterAdhesion(UUID id, RejectAdhesionRequest request) {
                StructureSanitaireDetail result = organisationFacade
                                .rejeterAdhesion(new RejectAdhesionCommand(id, request.motif()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ADHESION_REJECTED",
                                "Demande d'adhésion rejetée"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> activer(UUID id) {
                StructureSanitaireDetail result = organisationFacade
                                .activerStructureSanitaire(new ActivateStructureSanitaireCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "STRUCTURE_SANITAIRE_ACTIVATED", "Structure sanitaire activée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> desactiver(UUID id) {
                StructureSanitaireDetail result = organisationFacade
                                .desactiverStructureSanitaire(new DeactivateStructureSanitaireCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "STRUCTURE_SANITAIRE_DEACTIVATED", "Structure sanitaire désactivée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> affecterRegion(UUID id, AssignRegionRequest request) {
                StructureSanitaireDetail result = organisationFacade
                                .affecterStructureARegion(new AssignStructureToRegionCommand(id, request.regionId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "STRUCTURE_SANITAIRE_REGION_ASSIGNED",
                                "Structure sanitaire rattachée à la région avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> affecterPra(UUID id, AssignPraRequest request) {
                StructureSanitaireDetail result = organisationFacade
                                .affecterStructureAPra(new AssignStructureToPraCommand(id, request.praId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "STRUCTURE_SANITAIRE_PRA_ASSIGNED",
                                "Structure sanitaire rattachée à la PRA avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                StructureSanitaireDetail result = organisationFacade
                                .obtenirStructureSanitaire(new GetStructureSanitaireQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "STRUCTURE_SANITAIRE_FOUND", "Structure sanitaire récupérée"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> lister(
                        String q, TypeStructureSanitaire type, UUID regionId, UUID praId,
                        StatutAdhesion statutAdhesion, Boolean actif, Integer page, Integer size,
                        String sortBy, String sortDirection) {

                StructureSanitairePage result = organisationFacade.listerStructuresSanitaires(
                                new ListStructuresSanitairesQuery(q, type, regionId, praId, statutAdhesion, actif, page,
                                                size,
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
                                detail.regionId(), detail.regionNom(), detail.praId(), detail.praNom(),
                                detail.district(),
                                detail.adresse(), detail.telephone(), detail.email(), detail.responsableNom(),
                                detail.responsablePrenom(), detail.statutAdhesion(), detail.motifRejet(),
                                detail.actif(),
                                detail.createdAt(), detail.updatedAt());
        }
}
