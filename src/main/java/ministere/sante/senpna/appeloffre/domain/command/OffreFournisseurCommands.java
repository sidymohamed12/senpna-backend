package ministere.sante.senpna.appeloffre.domain.command;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OffreFournisseurCommands {

    private OffreFournisseurCommands() {
    }

    // ── Commandes (fournisseur) ──────────────────────────────────────────

    public record SoumettreOffreCommand(UUID appelOffreId, UUID fournisseurId, String commentaire,
            List<LigneOffreInput> lignes) {
    }

    public record LigneOffreInput(UUID ligneAppelOffreId, BigDecimal prixUnitaire, Integer delaiLivraisonJours) {
    }

    public record RetirerOffreCommand(UUID offreId, UUID fournisseurId) {
    }

    // ── Commandes (PNA) ──────────────────────────────────────────────────

    public record RetenirOffreCommand(UUID offreId) {
    }

    public record RejeterOffreCommand(UUID offreId) {
    }

    // ── Requêtes ─────────────────────────────────────────────────────────

    public record GetOffreQuery(UUID offreId, UUID fournisseurId) {
    }

    public record ListOffresAppelOffreQuery(UUID appelOffreId, Integer page, Integer size) {
    }

    public record ListMesOffresQuery(UUID fournisseurId, StatutOffre statut, Integer page, Integer size) {
    }

    // ── Résultats ────────────────────────────────────────────────────────

    public record LigneOffreDetail(UUID id, UUID ligneAppelOffreId, BigDecimal prixUnitaire,
            Integer delaiLivraisonJours) {
    }

    public record OffreDetail(UUID id, UUID appelOffreId, UUID fournisseurId, String commentaire,
            StatutOffre statut, List<LigneOffreDetail> lignes, Instant createdAt, Instant updatedAt) {
    }

    public record OffrePage(List<OffreDetail> content, int page, int size, long totalElements, int totalPages) {
    }
}
