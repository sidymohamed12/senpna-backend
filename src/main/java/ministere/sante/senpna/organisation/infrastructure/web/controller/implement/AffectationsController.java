package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToEntrepotCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToStructureCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UnassignUserCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;
import ministere.sante.senpna.organisation.infrastructure.web.controller.IAffectationsController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToEntrepotRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToStructureRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.UserAffectationResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole('ADMIN_PNA','ADMIN_PRA')")
public class AffectationsController implements IAffectationsController {

        private final OrganisationFacade organisationFacade;

        public AffectationsController(OrganisationFacade organisationFacade) {
                this.organisationFacade = organisationFacade;
        }

        @Override
        public ResponseEntity<Map<String, Object>> affecterAEntrepot(UUID userId, AssignUserToEntrepotRequest request) {
                UserAffectationDetail result = organisationFacade
                                .affecterUtilisateurAEntrepot(
                                                new AssignUserToEntrepotCommand(userId, request.entrepotId()));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_ASSIGNED_ENTREPOT",
                                                "Utilisateur affecté à l'entrepôt avec succès"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> affecterAStructure(UUID userId,
                        AssignUserToStructureRequest request) {
                UserAffectationDetail result = organisationFacade.affecterUtilisateurAStructure(
                                new AssignUserToStructureCommand(userId, request.structureSanitaireId()));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_ASSIGNED_STRUCTURE",
                                                "Utilisateur affecté à la structure sanitaire avec succès"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> retirerAffectation(UUID userId) {
                UserAffectationDetail result = organisationFacade
                                .retirerAffectationUtilisateur(new UnassignUserCommand(userId));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_UNASSIGNED",
                                "Affectation de l'utilisateur retirée avec succès"));
        }

        private UserAffectationResponse toResponse(UserAffectationDetail detail) {
                return new UserAffectationResponse(detail.userId(), detail.entrepotId(), detail.structureSanitaireId());
        }
}
