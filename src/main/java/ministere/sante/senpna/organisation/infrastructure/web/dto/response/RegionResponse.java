package ministere.sante.senpna.organisation.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RegionResponse(UUID id, String code, String nom, boolean actif, Instant createdAt,
        Instant updatedAt) {
}
