package ministere.sante.senpna.organisation.infrastructure.web.dto.response;

import java.util.UUID;

public record UserAffectationResponse(UUID userId, UUID entrepotId, UUID structureSanitaireId) {
}
