package ministere.sante.senpna.organisation.infrastructure.web.dto.response;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;

import java.time.Instant;
import java.util.UUID;

public record EntrepotResponse(
        UUID id,
        String code,
        String nom,
        TypeEntrepot type,
        UUID regionId,
        String regionNom,
        String adresse,
        String telephone,
        UUID responsableUserId,
        boolean actif,
        Instant createdAt,
        Instant updatedAt) {
}
