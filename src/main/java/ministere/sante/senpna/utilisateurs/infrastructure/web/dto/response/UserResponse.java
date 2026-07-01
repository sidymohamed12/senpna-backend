package ministere.sante.senpna.utilisateurs.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
                UUID id,
                String nom,
                String prenom,
                String email,
                String telephone,
                boolean actif,
                Set<RoleSummaryResponse> roles,
                Instant createdAt,
                Instant updatedAt) {
}
