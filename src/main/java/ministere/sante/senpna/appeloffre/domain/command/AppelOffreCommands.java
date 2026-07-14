package ministere.sante.senpna.appeloffre.domain.command;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AppelOffreCommands {

    private AppelOffreCommands() {
    }

    // ── Commandes (PNA) ──────────────────────────────────────────────────

    public record CreateAppelOffreCommand(String reference, String objet, LocalDate dateCloture,
            List<LigneAppelOffreInput> lignes) {
    }

    public record LigneAppelOffreInput(UUID medicamentId, String designation, BigDecimal quantiteEstimee,
            String uniteBase) {
    }

    public record PublierAppelOffreCommand(UUID appelOffreId) {
    }

    public record ClorerAppelOffreCommand(UUID appelOffreId) {
    }

    public record AnnulerAppelOffreCommand(UUID appelOffreId) {
    }

    public record AttribuerAppelOffreCommand(UUID appelOffreId, List<UUID> offresRetenuesIds,
            List<UUID> offresRejeteesIds) {
    }

    // ── Requêtes ─────────────────────────────────────────────────────────

    public record GetAppelOffreQuery(UUID appelOffreId) {
    }

    public record ListAppelOffresQuery(String recherche, StatutAppelOffre statut, Integer page, Integer size,
            String sortBy, String sortDirection) {
    }

    // ── Résultats ────────────────────────────────────────────────────────

    public record LigneAppelOffreDetail(UUID id, UUID medicamentId, String designation, BigDecimal quantiteEstimee,
            String uniteBase) {
    }

    public record AppelOffreDetail(UUID id, String reference, String objet, LocalDate dateCloture,
            StatutAppelOffre statut, List<LigneAppelOffreDetail> lignes, Instant createdAt, Instant updatedAt) {
    }

    public record AppelOffreSummary(UUID id, String reference, String objet, LocalDate dateCloture,
            StatutAppelOffre statut, int nombreLignes, Instant createdAt) {
    }

    public record AppelOffrePage(List<AppelOffreSummary> content, int page, int size, long totalElements,
            int totalPages) {
    }
}
