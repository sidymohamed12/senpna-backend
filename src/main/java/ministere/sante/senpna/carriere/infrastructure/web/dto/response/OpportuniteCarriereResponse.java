package ministere.sante.senpna.carriere.infrastructure.web.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OpportuniteCarriereResponse(
        UUID id,
        String titre,
        String nomEntreprise,
        String description,
        String ficheDePosteUrl,
        String lieu,
        String typeContrat,
        LocalDate dateDebut,
        LocalDate dateLimiteCandidature,
        UUID auteurId,
        String auteurNom,
        String emailContact,
        String statut,
        Instant createdAt,
        Instant updatedAt) {
}
