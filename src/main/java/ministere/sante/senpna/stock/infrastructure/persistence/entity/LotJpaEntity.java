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

    public LotJpaEntity(UUID id, String numeroLot, UUID medicamentId, UUID fournisseurId, LocalDate dateFabrication,
            LocalDate dateExpiration, BigDecimal prixAchat, BigDecimal prixVente, String statut) {
        super(id);
        this.numeroLot = numeroLot;
        this.medicamentId = medicamentId;
        this.fournisseurId = fournisseurId;
        this.dateFabrication = dateFabrication;
        this.dateExpiration = dateExpiration;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.statut = statut;
    }
}
