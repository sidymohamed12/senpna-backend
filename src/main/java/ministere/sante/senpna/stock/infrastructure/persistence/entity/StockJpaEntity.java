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
import java.util.UUID;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class StockJpaEntity extends BaseJpaEntity {

    @Column(name = "entrepot_id", nullable = false)
    private UUID entrepotId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "medicament_id", nullable = false)
    private UUID medicamentId;

    @Column(name = "quantite_disponible", nullable = false, precision = 14, scale = 4)
    private BigDecimal quantiteDisponible;

    @Column(name = "quantite_reservee", nullable = false, precision = 14, scale = 4)
    private BigDecimal quantiteReservee;

    @Column(name = "quantite_en_commande", nullable = false, precision = 14, scale = 4)
    private BigDecimal quantiteEnCommande;

    @Column(name = "seuil_alerte", precision = 14, scale = 4)
    private BigDecimal seuilAlerte;

    private StockJpaEntity(Builder builder) {
        super(builder.id);
        this.entrepotId = builder.entrepotId;
        this.lotId = builder.lotId;
        this.medicamentId = builder.medicamentId;
        this.quantiteDisponible = builder.quantiteDisponible;
        this.quantiteReservee = builder.quantiteReservee;
        this.quantiteEnCommande = builder.quantiteEnCommande;
        this.seuilAlerte = builder.seuilAlerte;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private UUID entrepotId;
        private UUID lotId;
        private UUID medicamentId;
        private BigDecimal quantiteDisponible;
        private BigDecimal quantiteReservee;
        private BigDecimal quantiteEnCommande;
        private BigDecimal seuilAlerte;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder entrepotId(UUID entrepotId) {
            this.entrepotId = entrepotId;
            return this;
        }

        public Builder lotId(UUID lotId) {
            this.lotId = lotId;
            return this;
        }

        public Builder medicamentId(UUID medicamentId) {
            this.medicamentId = medicamentId;
            return this;
        }

        public Builder quantiteDisponible(BigDecimal quantiteDisponible) {
            this.quantiteDisponible = quantiteDisponible;
            return this;
        }

        public Builder quantiteReservee(BigDecimal quantiteReservee) {
            this.quantiteReservee = quantiteReservee;
            return this;
        }

        public Builder quantiteEnCommande(BigDecimal quantiteEnCommande) {
            this.quantiteEnCommande = quantiteEnCommande;
            return this;
        }

        public Builder seuilAlerte(BigDecimal seuilAlerte) {
            this.seuilAlerte = seuilAlerte;
            return this;
        }

        public StockJpaEntity build() {
            return new StockJpaEntity(this);
        }
    }

}
