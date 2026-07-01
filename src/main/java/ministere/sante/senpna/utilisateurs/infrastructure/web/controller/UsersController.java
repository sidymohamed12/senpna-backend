package ministere.sante.senpna.utilisateurs.infrastructure.web.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Endpoints d'administration des comptes utilisateurs : création,
 * consultation, modification, activation/désactivation et gestion des
 * rôles (RBAC).
 *
 * <p>
 * Réservé aux rôles {@code ADMIN_PNA} et {@code ADMIN_PRA} (cf. section 4
 * du modèle métier — "toutes les fonctionnalités PNA/PRA, gestion des
 * comptes, gestion des rôles").
 * </p>
 *
 * <h3>Routes</h3>
 * 
 * <pre>
 * POST   /api/users                     {nom,prenom,email,telephone?,roleIds}
 * GET    /api/users?q=&actif=&roleId=&page=&size=&sortBy=&sortDirection=
 * GET    /api/users/{id}
 * PUT    /api/users/{id}                 {nom,prenom,telephone?}
 * PATCH  /api/users/{id}/activer
 * PATCH  /api/users/{id}/desactiver
 * POST   /api/users/{id}/roles           {roleId}
 * DELETE /api/users/{id}/roles/{roleId}
 * </pre>
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('ADMIN_PNA','ADMIN_PRA')")
public class UsersController {

        private final UserManagementFacade userManagementFacade;

        public UsersController(UserManagementFacade userManagementFacade) {
                this.userManagementFacade = userManagementFacade;
        }

        @PostMapping
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateUserRequest request) {
                CreatedUser result = userManagementFacade.creer(new CreateUserCommand(
                                request.nom(), request.prenom(), request.email(), request.telephone(),
                                request.roleIds()));

                CreatedUserResponse body = new CreatedUserResponse(toResponse(result.user()),
                                result.motDePasseTemporaire());

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, body, "USER_CREATED",
                                                "Utilisateur créé avec succès"));
        }

        @GetMapping
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(required = false) UUID roleId,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

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

        @GetMapping("/{id}")
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                UserDetail result = userManagementFacade.obtenir(new GetUserQuery(id));
                return ResponseEntity.ok(
                                RestResponse.response(HttpStatus.OK, toResponse(result), "USER_FOUND",
                                                "Utilisateur récupéré"));
        }

        @PutMapping("/{id}")
        public ResponseEntity<Map<String, Object>> modifier(
                        @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
                UserDetail result = userManagementFacade.modifier(
                                new UpdateUserCommand(id, request.nom(), request.prenom(), request.telephone()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_UPDATED",
                                "Utilisateur modifié avec succès"));
        }

        @PatchMapping("/{id}/activer")
        public ResponseEntity<Map<String, Object>> activer(@PathVariable UUID id) {
                UserDetail result = userManagementFacade.activer(new ActivateUserCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_ACTIVATED",
                                "Utilisateur activé avec succès"));
        }

        @PatchMapping("/{id}/desactiver")
        public ResponseEntity<Map<String, Object>> desactiver(@PathVariable UUID id) {
                UserDetail result = userManagementFacade.desactiver(new DeactivateUserCommand(id, currentUserId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_DEACTIVATED",
                                "Utilisateur désactivé avec succès"));
        }

        @PostMapping("/{id}/roles")
        public ResponseEntity<Map<String, Object>> assignerRole(
                        @PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {
                UserDetail result = userManagementFacade.assignerRole(new AssignRoleCommand(id, request.roleId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ROLE_ASSIGNED",
                                "Rôle attribué avec succès"));
        }

        @DeleteMapping("/{id}/roles/{roleId}")
        public ResponseEntity<Map<String, Object>> retirerRole(@PathVariable UUID id, @PathVariable UUID roleId) {
                UserDetail result = userManagementFacade.retirerRole(new RevokeRoleCommand(id, roleId));
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
                                detail.actif(), roles, detail.createdAt(), detail.updatedAt());
        }

        private RoleSummaryResponse toResponse(RoleSummary summary) {
                return new RoleSummaryResponse(summary.id(), summary.code(), summary.nom());
        }
}
