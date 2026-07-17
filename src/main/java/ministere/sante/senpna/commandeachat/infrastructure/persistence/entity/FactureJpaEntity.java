package ministere.sante.senpna.commandeachat.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "factures")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class FactureJpaEntity extends BaseJpaEntity {

    @Column(name = "commande_achat_id", nullable = false)
    private UUID commandeAchatId;

    @Column(name = "fournisseur_id", nullable = false)
    private UUID fournisseurId;

    @Column(name = "numero_facture", nullable = false, length = 50)
    private String numeroFacture;

    @Column(name = "montant", nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_emission", nullable = false)
    private LocalDate dateEmission;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "piece_jointe_media_id", length = 100)
    private String pieceJointeMediaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutFacture statut;

    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    private FactureJpaEntity(Builder builder) {
        super(builder.id);
        this.commandeAchatId = builder.commandeAchatId;
        this.fournisseurId = builder.fournisseurId;
        this.numeroFacture = builder.numeroFacture;
        this.montant = builder.montant;
        this.dateEmission = builder.dateEmission;
        this.dateEcheance = builder.dateEcheance;
        this.pieceJointeMediaId = builder.pieceJointeMediaId;
        this.statut = builder.statut;
        this.motifRejet = builder.motifRejet;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private UUID commandeAchatId;
        private UUID fournisseurId;
        private String numeroFacture;
        private BigDecimal montant;
        private LocalDate dateEmission;
        private LocalDate dateEcheance;
        private String pieceJointeMediaId;
        private StatutFacture statut;
        private String motifRejet;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder commandeAchatId(UUID commandeAchatId) {
            this.commandeAchatId = commandeAchatId;
            return this;
        }

        public Builder fournisseurId(UUID fournisseurId) {
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

        public FactureJpaEntity build() {
            return new FactureJpaEntity(this);
        }
    }

}
