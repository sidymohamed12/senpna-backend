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

    public LigneCommandeAchatJpaEntity(UUID id, UUID commandeAchatId, UUID medicamentId, UUID conditionnementId,
            BigDecimal quantiteCommandee, BigDecimal prixUnitaire, String numeroLot, LocalDate dateFabrication,
            LocalDate dateExpiration, String certificatAnalyseUrl, BigDecimal quantiteExpediee,
            BigDecimal quantiteRecue, BigDecimal quantiteRefusee, String motifRefus) {
        super(id);
        this.commandeAchatId = commandeAchatId;
        this.medicamentId = medicamentId;
        this.conditionnementId = conditionnementId;
        this.quantiteCommandee = quantiteCommandee;
        this.prixUnitaire = prixUnitaire;
        this.numeroLot = numeroLot;
        this.dateFabrication = dateFabrication;
        this.dateExpiration = dateExpiration;
        this.certificatAnalyseUrl = certificatAnalyseUrl;
        this.quantiteExpediee = quantiteExpediee;
        this.quantiteRecue = quantiteRecue;
        this.quantiteRefusee = quantiteRefusee;
        this.motifRefus = motifRefus;
    }
}
