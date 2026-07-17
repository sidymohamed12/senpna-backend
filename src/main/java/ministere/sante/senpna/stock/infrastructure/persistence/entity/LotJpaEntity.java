package ministere.sante.senpna.stock.infrastructure.persistence.entity;

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
@Table(name = "lots")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class LotJpaEntity extends BaseJpaEntity {

    @Column(name = "numero_lot", nullable = false, length = 50)
    private String numeroLot;

    @Column(name = "medicament_id", nullable = false)
    private UUID medicamentId;

    @Column(name = "fournisseur_id", nullable = false)
    private UUID fournisseurId;

    @Column(name = "date_fabrication")
    private LocalDate dateFabrication;

    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @Column(name = "prix_achat", precision = 14, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "prix_vente", precision = 14, scale = 2)
    private BigDecimal prixVente;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    private LotJpaEntity(Builder builder) {
        super(builder.id);
        this.numeroLot = builder.numeroLot;
        this.medicamentId = builder.medicamentId;
        this.fournisseurId = builder.fournisseurId;
        this.dateFabrication = builder.dateFabrication;
        this.dateExpiration = builder.dateExpiration;
        this.prixAchat = builder.prixAchat;
        this.prixVente = builder.prixVente;
        this.statut = builder.statut;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String numeroLot;
        private UUID medicamentId;
        private UUID fournisseurId;
        private LocalDate dateFabrication;
        private LocalDate dateExpiration;
        private BigDecimal prixAchat;
        private BigDecimal prixVente;
        private String statut;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder numeroLot(String numeroLot) {
            this.numeroLot = numeroLot;
            return this;
        }

        public Builder medicamentId(UUID medicamentId) {
            this.medicamentId = medicamentId;
            return this;
        }

        public Builder fournisseurId(UUID fournisseurId) {
            this.fournisseurId = fournisseurId;
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

        public Builder prixAchat(BigDecimal prixAchat) {
            this.prixAchat = prixAchat;
            return this;
        }

        public Builder prixVente(BigDecimal prixVente) {
            this.prixVente = prixVente;
            return this;
        }

        public Builder statut(String statut) {
            this.statut = statut;
            return this;
        }

        public LotJpaEntity build() {
            return new LotJpaEntity(this);
        }
    }

}
