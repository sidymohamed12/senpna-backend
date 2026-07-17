package ministere.sante.senpna.commandeachat.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "commande_achat_lignes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class LigneCommandeAchatJpaEntity extends BaseJpaEntity {

    @Column(name = "commande_achat_id", nullable = false)
    private UUID commandeAchatId;

    @Column(name = "medicament_id", nullable = false)
    private UUID medicamentId;

    @Column(name = "conditionnement_id", nullable = false)
    private UUID conditionnementId;

    @Column(name = "quantite_commandee", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantiteCommandee;

    @Column(name = "prix_unitaire", nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "numero_lot", length = 50)
    private String numeroLot;

    @Column(name = "date_fabrication")
    private LocalDate dateFabrication;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "certificat_analyse_url", length = 1000)
    private String certificatAnalyseUrl;

    @Column(name = "quantite_expediee", precision = 14, scale = 2)
    private BigDecimal quantiteExpediee;

    @Column(name = "quantite_recue", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantiteRecue;

    @Column(name = "quantite_refusee", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantiteRefusee;

    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    private LigneCommandeAchatJpaEntity(Builder builder) {
        super(builder.id);
        this.commandeAchatId = builder.commandeAchatId;
        this.medicamentId = builder.medicamentId;
        this.conditionnementId = builder.conditionnementId;
        this.quantiteCommandee = builder.quantiteCommandee;
        this.prixUnitaire = builder.prixUnitaire;
        this.numeroLot = builder.numeroLot;
        this.dateFabrication = builder.dateFabrication;
        this.dateExpiration = builder.dateExpiration;
        this.certificatAnalyseUrl = builder.certificatAnalyseUrl;
        this.quantiteExpediee = builder.quantiteExpediee;
        this.quantiteRecue = builder.quantiteRecue;
        this.quantiteRefusee = builder.quantiteRefusee;
        this.motifRefus = builder.motifRefus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private UUID commandeAchatId;
        private UUID medicamentId;
        private UUID conditionnementId;
        private BigDecimal quantiteCommandee;
        private BigDecimal prixUnitaire;
        private String numeroLot;
        private LocalDate dateFabrication;
        private LocalDate dateExpiration;
        private String certificatAnalyseUrl;
        private BigDecimal quantiteExpediee;
        private BigDecimal quantiteRecue;
        private BigDecimal quantiteRefusee;
        private String motifRefus;

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

        public Builder medicamentId(UUID medicamentId) {
            this.medicamentId = medicamentId;
            return this;
        }

        public Builder conditionnementId(UUID conditionnementId) {
            this.conditionnementId = conditionnementId;
            return this;
        }

        public Builder quantiteCommandee(BigDecimal quantiteCommandee) {
            this.quantiteCommandee = quantiteCommandee;
            return this;
        }

        public Builder prixUnitaire(BigDecimal prixUnitaire) {
            this.prixUnitaire = prixUnitaire;
            return this;
        }

        public Builder numeroLot(String numeroLot) {
            this.numeroLot = numeroLot;
            return this;
        }

        public Builder dateFabrication(LocalDate dateFabrication) {
            this.dateFabrication = dateFabrication;
            return this;
        }

        public Builder dateExpiration(LocalDate dateExpiration) {
            this.dateExpiration = dateExpiration;
            return this;
        }

        public Builder certificatAnalyseUrl(String certificatAnalyseUrl) {
            this.certificatAnalyseUrl = certificatAnalyseUrl;
            return this;
        }

        public Builder quantiteExpediee(BigDecimal quantiteExpediee) {
            this.quantiteExpediee = quantiteExpediee;
            return this;
        }

        public Builder quantiteRecue(BigDecimal quantiteRecue) {
            this.quantiteRecue = quantiteRecue;
            return this;
        }

        public Builder quantiteRefusee(BigDecimal quantiteRefusee) {
            this.quantiteRefusee = quantiteRefusee;
            return this;
        }

        public Builder motifRefus(String motifRefus) {
            this.motifRefus = motifRefus;
            return this;
        }

        public LigneCommandeAchatJpaEntity build() {
            return new LigneCommandeAchatJpaEntity(this);
        }
    }

}
