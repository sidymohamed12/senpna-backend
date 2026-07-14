package ministere.sante.senpna.appeloffre.infrastructure.web.dto.response;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AppelOffreSummaryResponse(UUID id, String reference, String objet, LocalDate dateCloture,
        StatutAppelOffre statut, int nombreLignes, Instant createdAt) {
}
