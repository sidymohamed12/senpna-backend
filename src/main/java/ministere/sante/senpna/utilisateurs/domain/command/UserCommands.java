package ministere.sante.senpna.utilisateurs.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class UserCommands {

        private UserCommands() {
        }

        // ── Commandes ───────────────────────────────────────────────────────

        public record CreateUserCommand(UUID acteurId, String nom, String prenom, String email, String telephone,
                        Set<UUID> roleIds) {
        }

        public record UpdateUserCommand(UUID userId, UUID acteurId, String nom, String prenom, String telephone) {
        }

        public record ActivateUserCommand(UUID userId, UUID acteurId) {
        }

        public record DeactivateUserCommand(UUID userId, UUID acteurId) {
        }

        public record AssignRoleCommand(UUID userId, UUID acteurId, UUID roleId) {
        }

        public record RevokeRoleCommand(UUID userId, UUID acteurId, UUID roleId) {
        }

        // ── Queries ─────────────────────────────────────────────────────────

        public record GetUserQuery(UUID userId) {
        }

        public record ListUsersQuery(String recherche, Boolean actif, UUID roleId, Integer page, Integer size,
                        String sortBy, String sortDirection) {
        }

        // ── Résultats ───────────────────────────────────────────────────────

        public record RoleSummary(UUID id, String code, String nom) {
        }

        public record UserDetail(UUID id, String nom, String prenom, String email, String telephone, boolean actif,
                        Set<RoleSummary> roles, Instant createdAt, Instant updatedAt) {
        }

        public record CreatedUser(UserDetail user, String motDePasseTemporaire) {
        }

        public record UserPage(List<UserDetail> content, int page, int size, long totalElements, int totalPages) {
        }
}
