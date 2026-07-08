package ministere.sante.senpna.carriere.domain.command;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class OpportuniteCarriereCommands {

    private OpportuniteCarriereCommands() {
    }

    // ── Commandes ────────────────────────────────────────────────────────

    /**
     * @param ficheDePosteUrl URL publique optionnelle, déjà uploadée par
     *                        le front vers le stockage objet via le flux
     *                        Presigned URL (cf.
     *                        {@code POST /api/medias/presigned-url} avec
     *                        {@code mediaType=FICHE_DE_POSTE}) — l'API ne
     *                        reçoit et ne manipule jamais les octets du
     *                        fichier.
     */
    public record CreateOpportuniteCarriereCommand(
            UUID auteurId,
            String titre,
            String nomEntreprise,
            String description,
            String ficheDePosteUrl,
            String lieu,
            String typeContrat,
            LocalDate dateDebut,
            LocalDate dateLimiteCandidature,
            String emailContact) {
    }

    public record UpdateOpportuniteCarriereCommand(
            UUID opportuniteId,
            String titre,
            String nomEntreprise,
            String description,
            String ficheDePosteUrl,
            String lieu,
            String typeContrat,
            LocalDate dateDebut,
            LocalDate dateLimiteCandidature,
            String emailContact) {
    }

    public record PublierOpportuniteCommand(UUID opportuniteId) {
    }

    public record MettreEnCoursOpportuniteCommand(UUID opportuniteId) {
    }

    public record CloturerOpportuniteCommand(UUID opportuniteId) {
    }

    public record RemettreEnBrouillonOpportuniteCommand(UUID opportuniteId) {
    }

    // ── Requêtes ─────────────────────────────────────────────────────────

    public record GetOpportuniteCarriereQuery(UUID opportuniteId) {
    }

    public record ListOpportunitesCarriereQuery(
            String recherche,
            String typeContrat,
            String statut,
            boolean publicOnly,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection) {
    }

    // ── Résultats ────────────────────────────────────────────────────────

    public record OpportuniteCarriereDetail(
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

    public record OpportuniteCarrierePage(
            List<OpportuniteCarriereDetail> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {
    }
}
