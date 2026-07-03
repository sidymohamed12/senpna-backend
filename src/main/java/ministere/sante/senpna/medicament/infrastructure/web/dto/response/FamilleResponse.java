package ministere.sante.senpna.medicament.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FamilleResponse(
        UUID id,
        String code,
        String libelle,
        String description,
        boolean actif,
        Instant createdAt,
        Instant updatedAt) {
}
