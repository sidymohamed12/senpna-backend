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

    public StockJpaEntity(UUID id, UUID entrepotId, UUID lotId, UUID medicamentId, BigDecimal quantiteDisponible,
            BigDecimal quantiteReservee, BigDecimal quantiteEnCommande, BigDecimal seuilAlerte) {
        super(id);
        this.entrepotId = entrepotId;
        this.lotId = lotId;
        this.medicamentId = medicamentId;
        this.quantiteDisponible = quantiteDisponible;
        this.quantiteReservee = quantiteReservee;
        this.quantiteEnCommande = quantiteEnCommande;
        this.seuilAlerte = seuilAlerte;
    }
}
