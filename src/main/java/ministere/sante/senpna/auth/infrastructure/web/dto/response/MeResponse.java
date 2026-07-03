package ministere.sante.senpna.auth.infrastructure.web.dto.response;

import java.util.Set;
import java.util.UUID;

public record MeResponse(UUID id, String nom, String prenom, String email, Set<String> roles, UUID entrepotId,
        UUID structureSanitaireId) {
}
