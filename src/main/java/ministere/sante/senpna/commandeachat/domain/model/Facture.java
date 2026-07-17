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

    private Facture(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.commandeAchatId = Objects.requireNonNull(builder.commandeAchatId, "La commande référencée est obligatoire");
        this.fournisseurId = Objects.requireNonNull(builder.fournisseurId, "Le fournisseur est obligatoire");
        this.numeroFacture = validerNumeroFacture(builder.numeroFacture);
        this.montant = validerMontant(builder.montant);
        this.dateEmission = Objects.requireNonNull(builder.dateEmission, "La date d'émission est obligatoire");
        this.dateEcheance = builder.dateEcheance;
        this.pieceJointeMediaId = builder.pieceJointeMediaId;
        this.statut = Objects.requireNonNull(builder.statut, "Le statut est obligatoire");
        this.motifRejet = builder.motifRejet;
    }

    /** Données nécessaires à la soumission d'une nouvelle facture. */
    public record SoumissionCommand(CommandeAchatId commandeAchatId, FournisseurId fournisseurId,
            String numeroFacture, BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance,
            String pieceJointeMediaId) {
    }

    public static Facture soumettre(SoumissionCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(FactureId.generate())
                .commandeAchatId(command.commandeAchatId())
                .fournisseurId(command.fournisseurId())
                .numeroFacture(command.numeroFacture())
                .montant(command.montant())
                .dateEmission(command.dateEmission())
                .dateEcheance(command.dateEcheance())
                .pieceJointeMediaId(command.pieceJointeMediaId())
                .statut(StatutFacture.SOUMISE)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private FactureId id;
        private CommandeAchatId commandeAchatId;
        private FournisseurId fournisseurId;
        private String numeroFacture;
        private BigDecimal montant;
        private LocalDate dateEmission;
        private LocalDate dateEcheance;
        private String pieceJointeMediaId;
        private StatutFacture statut;
        private String motifRejet;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(FactureId id) {
            this.id = id;
            return this;
        }

        public Builder commandeAchatId(CommandeAchatId commandeAchatId) {
            this.commandeAchatId = commandeAchatId;
            return this;
        }

        public Builder fournisseurId(FournisseurId fournisseurId) {
            this.fournisseurId = fournisseurId;
            return this;
        }

        public Builder numeroFacture(String numeroFacture) {
            this.numeroFacture = numeroFacture;
            return this;
        }

        public Builder montant(BigDecimal montant) {
            this.montant = montant;
            return this;
        }

        public Builder dateEmission(LocalDate dateEmission) {
            this.dateEmission = dateEmission;
            return this;
        }

        public Builder dateEcheance(LocalDate dateEcheance) {
            this.dateEcheance = dateEcheance;
            return this;
        }

        public Builder pieceJointeMediaId(String pieceJointeMediaId) {
            this.pieceJointeMediaId = pieceJointeMediaId;
            return this;
        }

        public Builder statut(StatutFacture statut) {
            this.statut = statut;
            return this;
        }

        public Builder motifRejet(String motifRejet) {
            this.motifRejet = motifRejet;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Facture build() {
            return new Facture(this);
        }
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
