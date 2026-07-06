package ministere.sante.senpna.projet.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjetResponse(
        UUID id,
        String categorie,
        String nom,
        String description,
        List<String> objectifs,
        List<String> impacts,
        String imageUrl,
        String statut,
        Instant createdAt,
        Instant updatedAt) {
}
