package ministere.sante.senpna.actualite.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ActualiteResponse(
        UUID id,
        String categorie,
        String titre,
        String description,
        List<MediaResponse> medias,
        UUID auteurId,
        String auteurNom,
        List<String> tags,
        String statut,
        Instant createdAt,
        Instant updatedAt) {
}
