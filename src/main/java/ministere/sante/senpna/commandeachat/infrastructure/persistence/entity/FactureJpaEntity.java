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

    public FactureJpaEntity(UUID id, UUID commandeAchatId, UUID fournisseurId, String numeroFacture,
            BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance, String pieceJointeMediaId,
            StatutFacture statut, String motifRejet) {
        super(id);
        this.commandeAchatId = commandeAchatId;
        this.fournisseurId = fournisseurId;
        this.numeroFacture = numeroFacture;
        this.montant = montant;
        this.dateEmission = dateEmission;
        this.dateEcheance = dateEcheance;
        this.pieceJointeMediaId = pieceJointeMediaId;
        this.statut = statut;
        this.motifRejet = motifRejet;
    }
}
