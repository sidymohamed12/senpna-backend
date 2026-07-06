package ministere.sante.senpna.utilisateurs.infrastructure.web.controller.implement;

import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;
import ministere.sante.senpna.utilisateurs.application.facade.UserManagementFacade;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ActivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.AssignRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.DeactivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.GetUserQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ListUsersQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RevokeRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RoleSummary;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UpdateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserPage;
import ministere.sante.senpna.utilisateurs.infrastructure.web.controller.IUsersController;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.AssignRoleRequest;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.CreateUserRequest;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.UpdateUserRequest;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.response.CreatedUserResponse;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.response.RoleSummaryResponse;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.response.UserResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('ADMIN_PNA','ADMIN_PRA')")
public class UsersController implements IUsersController {

        private final UserManagementFacade userManagementFacade;

        public UsersController(UserManagementFacade userManagementFacade) {
                this.userManagementFacade = userManagementFacade;
        }

        @Override
        public ResponseEntity<Map<String, Object>> creer(CreateUserRequest request) {
                CreatedUser result = userManagementFacade.creer(new CreateUserCommand(
                                currentUserId(), request.nom(), request.prenom(), request.email(),
                                request.telephone(), request.roleIds(), request.entrepotId()));

                CreatedUserResponse body = new CreatedUserResponse(toResponse(result.user()),
                                result.motDePasseTemporaire());

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, body, "USER_CREATED",
                                                "Utilisateur créé avec succès"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> lister(
                        String q, Boolean actif, UUID roleId, Integer page, Integer size,
                        String sortBy, String sortDirection) {

                UserPage result = userManagementFacade.lister(
                                new ListUsersQuery(q, actif, roleId, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "USERS_LISTED",
                                "Liste des utilisateurs récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        @Override
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                UserDetail result = userManagementFacade.obtenir(new GetUserQuery(id));
                return ResponseEntity.ok(
                                RestResponse.response(HttpStatus.OK, toResponse(result), "USER_FOUND",
                                                "Utilisateur récupéré"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateUserRequest request) {
                UserDetail result = userManagementFacade.modifier(
                                new UpdateUserCommand(id, currentUserId(), request.nom(), request.prenom(),
                                                request.telephone()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_UPDATED",
                                "Utilisateur modifié avec succès"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> activer(UUID id) {
                UserDetail result = userManagementFacade.activer(new ActivateUserCommand(id, currentUserId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_ACTIVATED",
                                "Utilisateur activé avec succès"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> desactiver(UUID id) {
                UserDetail result = userManagementFacade.desactiver(new DeactivateUserCommand(id, currentUserId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_DEACTIVATED",
                                "Utilisateur désactivé avec succès"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> assignerRole(UUID id, AssignRoleRequest request) {
                UserDetail result = userManagementFacade.assignerRole(
                                new AssignRoleCommand(id, currentUserId(), request.roleId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ROLE_ASSIGNED",
                                "Rôle attribué avec succès"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> retirerRole(UUID id, UUID roleId) {
                UserDetail result = userManagementFacade.retirerRole(
                                new RevokeRoleCommand(id, currentUserId(), roleId));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ROLE_REVOKED",
                                "Rôle retiré avec succès"));
        }

        // ── Helpers ─────────────────────────────────────────────────────────

        private UUID currentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CurrentUser principal = (CurrentUser) authentication.getPrincipal();
                return principal.getUserId();
        }

        private UserResponse toResponse(UserDetail detail) {
                Set<RoleSummaryResponse> roles = detail.roles().stream()
                                .map(this::toResponse)
                                .collect(Collectors.toUnmodifiableSet());
                return new UserResponse(detail.id(), detail.nom(), detail.prenom(), detail.email(), detail.telephone(),
                                detail.actif(), roles, detail.entrepotId(), detail.structureSanitaireId(),
                                detail.createdAt(), detail.updatedAt());
        }

        private RoleSummaryResponse toResponse(RoleSummary summary) {
                return new RoleSummaryResponse(summary.id(), summary.code(), summary.nom());
        }
}
