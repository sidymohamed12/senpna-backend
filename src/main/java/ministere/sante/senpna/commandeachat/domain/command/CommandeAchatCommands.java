package ministere.sante.senpna.commandeachat.domain.command;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class CommandeAchatCommands {

    private CommandeAchatCommands() {
    }

    // ── Commandes (PNA) ──────────────────────────────────────────────────

    public record CreateCommandeAchatCommand(String reference, UUID fournisseurId, UUID entrepotDestinationId,
            String commentaire, List<LigneCommandeAchatInput> lignes) {
    }

    public record LigneCommandeAchatInput(UUID medicamentId, UUID conditionnementId, BigDecimal quantiteCommandee,
            BigDecimal prixUnitaire) {
    }

    public record ValiderCommandeAchatCommand(UUID commandeAchatId) {
    }

    public record RejeterCommandeAchatCommand(UUID commandeAchatId, String motif) {
    }

    public record AnnulerCommandeAchatCommand(UUID commandeAchatId) {
    }

    public record ReceptionnerCommandeAchatCommand(UUID commandeAchatId, List<InfoReceptionLigneInput> lignes) {
    }

    public record InfoReceptionLigneInput(UUID ligneId, BigDecimal quantiteRecue, BigDecimal quantiteRefusee,
            String motifRefus) {
    }

    // ── Commandes (fournisseur) ──────────────────────────────────────────

    public record AccuserReceptionCommandeCommand(UUID commandeAchatId, UUID fournisseurId) {
    }

    public record ConfirmerDelaiLivraisonCommand(UUID commandeAchatId, UUID fournisseurId, Integer delaiJours,
            LocalDate dateLivraisonConfirmee) {
    }

    public record GenererAvisExpeditionCommand(UUID commandeAchatId, UUID fournisseurId, LocalDate dateExpedition,
            String transporteur, String numeroSuivi, LocalDate dateLivraisonEstimee,
            List<InfoExpeditionLigneInput> lignes) {
    }

    public record InfoExpeditionLigneInput(UUID ligneId, String numeroLot, LocalDate dateFabrication,
            LocalDate dateExpiration, String certificatAnalyseUrl, BigDecimal quantiteExpediee) {
    }

    // ── Requêtes ─────────────────────────────────────────────────────────

    public record GetCommandeAchatQuery(UUID commandeAchatId, UUID fournisseurId) {
    }

    public record ListCommandeAchatQuery(String recherche, StatutCommandeAchat statut, Integer page, Integer size,
            String sortBy, String sortDirection) {
    }

    public record ListMesCommandesQuery(UUID fournisseurId, StatutCommandeAchat statut, Integer page, Integer size) {
    }

    // ── Résultats ────────────────────────────────────────────────────────

    public record LigneCommandeAchatDetail(UUID id, UUID medicamentId, UUID conditionnementId,
            BigDecimal quantiteCommandee, BigDecimal prixUnitaire, String numeroLot, LocalDate dateFabrication,
            LocalDate dateExpiration, String certificatAnalyseUrl, BigDecimal quantiteExpediee,
            BigDecimal quantiteRecue, BigDecimal quantiteRefusee, String motifRefus) {
    }

    public record AvisExpeditionDetail(LocalDate dateExpedition, String transporteur, String numeroSuivi,
            LocalDate dateLivraisonEstimee) {
    }

    public record CommandeAchatDetail(UUID id, String reference, UUID fournisseurId, UUID entrepotDestinationId,
            StatutCommandeAchat statut, List<LigneCommandeAchatDetail> lignes, Instant dateAccuseReceptionFournisseur,
            Integer delaiLivraisonConfirmeJours, LocalDate dateLivraisonConfirmee,
            AvisExpeditionDetail avisExpedition, String motifRejet, String commentaire, Instant createdAt,
            Instant updatedAt) {
    }

    public record CommandeAchatSummary(UUID id, String reference, UUID fournisseurId, StatutCommandeAchat statut,
            int nombreLignes, Instant createdAt) {
    }

    public record CommandeAchatPage(List<CommandeAchatSummary> content, int page, int size, long totalElements,
            int totalPages) {
    }
}
