package ministere.sante.senpna.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRoleRequest(
        @NotNull(message = "L'identifiant du rôle est obligatoire")
        UUID roleId) {
}
