package ministere.sante.senpna.appeloffre.infrastructure.web.dto.response;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AppelOffreResponse(UUID id, String reference, String objet, LocalDate dateCloture,
        StatutAppelOffre statut, List<LigneAppelOffreResponse> lignes, Instant createdAt, Instant updatedAt) {
}
