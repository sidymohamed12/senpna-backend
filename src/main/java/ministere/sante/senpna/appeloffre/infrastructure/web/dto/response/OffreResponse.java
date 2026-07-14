package ministere.sante.senpna.appeloffre.infrastructure.web.dto.response;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OffreResponse(UUID id, UUID appelOffreId, UUID fournisseurId, String commentaire, StatutOffre statut,
        List<LigneOffreResponse> lignes, Instant createdAt, Instant updatedAt) {
}
