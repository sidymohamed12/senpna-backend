package ministere.sante.senpna.commandeachat.domain.command;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class FactureCommands {

    private FactureCommands() {
    }

    // ── Commandes (fournisseur) ──────────────────────────────────────────

    public record SoumettreFactureCommand(UUID commandeAchatId, UUID fournisseurId, String numeroFacture,
            BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance, String pieceJointeMediaId) {
    }

    // ── Commandes (PNA) ──────────────────────────────────────────────────

    public record ValiderFactureCommand(UUID factureId) {
    }

    public record RejeterFactureCommand(UUID factureId, String motif) {
    }

    public record MarquerFacturePayeeCommand(UUID factureId) {
    }

    // ── Requêtes ─────────────────────────────────────────────────────────

    public record GetFactureQuery(UUID factureId, UUID fournisseurId) {
    }

    public record ListFacturesQuery(StatutFacture statut, Integer page, Integer size) {
    }

    public record ListMesFacturesQuery(UUID fournisseurId, StatutFacture statut, Integer page, Integer size) {
    }

    // ── Résultats ────────────────────────────────────────────────────────

    public record FactureDetail(UUID id, UUID commandeAchatId, UUID fournisseurId, String numeroFacture,
            BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance, String pieceJointeMediaId,
            StatutFacture statut, String motifRejet, Instant createdAt, Instant updatedAt) {
    }

    public record FacturePage(List<FactureDetail> content, int page, int size, long totalElements, int totalPages) {
    }
}
