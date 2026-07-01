package ministere.sante.senpna.auth.infrastructure.web.dto.response;

import java.util.UUID;

public record RoleSummaryResponse(UUID id, String code, String nom) {
}
