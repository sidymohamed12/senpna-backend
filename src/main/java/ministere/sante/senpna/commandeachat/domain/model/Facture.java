package ministere.sante.senpna.commandeachat.domain.model;

import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutFactureInvalideException;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Facture soumise par un fournisseur pour une commande d'achat livrée
 * (cf. doc. métier « Soumission de Factures » : dépôt et suivi, workflow
 * de validation, suivi du statut de paiement).
 *
 * <p>
 * Agrégat distinct de {@link CommandeAchat}, référencée par simple
 * {@code commandeAchatId} — une facture a son propre cycle de vie
 * (validation comptable, paiement) qui ne doit pas verrouiller la
 * commande sous-jacente.
 * </p>
 */
public class Facture extends AggregateRoot<FactureId> {

    private final CommandeAchatId commandeAchatId;
    private final FournisseurId fournisseurId;
    private String numeroFacture;
    private final BigDecimal montant;
    private final LocalDate dateEmission;
    private final LocalDate dateEcheance;
    private final String pieceJointeMediaId;
    private StatutFacture statut;
    private String motifRejet;

    private Facture(FactureId id, CommandeAchatId commandeAchatId, FournisseurId fournisseurId,
            String numeroFacture, BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance,
            String pieceJointeMediaId, StatutFacture statut, String motifRejet, Instant createdAt,
            Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.commandeAchatId = Objects.requireNonNull(commandeAchatId, "La commande référencée est obligatoire");
        this.fournisseurId = Objects.requireNonNull(fournisseurId, "Le fournisseur est obligatoire");
        this.numeroFacture = validerNumeroFacture(numeroFacture);
        this.montant = validerMontant(montant);
        this.dateEmission = Objects.requireNonNull(dateEmission, "La date d'émission est obligatoire");
        this.dateEcheance = dateEcheance;
        this.pieceJointeMediaId = pieceJointeMediaId;
        this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
        this.motifRejet = motifRejet;
    }

    public static Facture reconstruct(FactureId id, CommandeAchatId commandeAchatId, FournisseurId fournisseurId,
            String numeroFacture, BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance,
            String pieceJointeMediaId, StatutFacture statut, String motifRejet, Instant createdAt,
            Instant updatedAt) {
        return new Facture(id, commandeAchatId, fournisseurId, numeroFacture, montant, dateEmission, dateEcheance,
                pieceJointeMediaId, statut, motifRejet, createdAt, updatedAt);
    }

    public static Facture soumettre(CommandeAchatId commandeAchatId, FournisseurId fournisseurId,
            String numeroFacture, BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance,
            String pieceJointeMediaId) {
        Instant maintenant = Instant.now();
        return new Facture(FactureId.generate(), commandeAchatId, fournisseurId, numeroFacture, montant,
                dateEmission, dateEcheance, pieceJointeMediaId, StatutFacture.SOUMISE, null, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void valider() {
        exigerStatut(StatutFacture.SOUMISE, "valider");
        this.statut = StatutFacture.VALIDEE;
        markUpdated();
    }

    public void rejeter(String motif) {
        exigerStatut(StatutFacture.SOUMISE, "rejeter");
        this.motifRejet = motif;
        this.statut = StatutFacture.REJETEE;
        markUpdated();
    }

    public void marquerPayee() {
        exigerStatut(StatutFacture.VALIDEE, "marquer payée");
        this.statut = StatutFacture.PAYEE;
        markUpdated();
    }

    public boolean appartientA(FournisseurId candidat) {
        return this.fournisseurId.equals(candidat);
    }

    private void exigerStatut(StatutFacture attendu, String action) {
        if (this.statut != attendu) {
            throw new TransitionStatutFactureInvalideException(this.statut, action);
        }
    }

    private static String validerNumeroFacture(String numero) {
        Objects.requireNonNull(numero, "Le numéro de facture ne peut pas être null");
        String trimmed = numero.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le numéro de facture ne peut pas être vide");
        }
        return trimmed;
    }

    private static BigDecimal validerMontant(BigDecimal montant) {
        Objects.requireNonNull(montant, "Le montant est obligatoire");
        if (montant.signum() <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }
        return montant;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public CommandeAchatId getCommandeAchatId() {
        return commandeAchatId;
    }

    public FournisseurId getFournisseurId() {
        return fournisseurId;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public String getPieceJointeMediaId() {
        return pieceJointeMediaId;
    }

    public StatutFacture getStatut() {
        return statut;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
