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
import java.time.Instant;
import java.util.UUID;

/**
 * Journal des mouvements de stock — append-only (cf. doc. métier §13 et
 * §18). Aucune mise à jour n'est effectuée après création : seul
 * {@code save()} (insertion) est utilisé par l'adaptateur.
 */
@Entity
@Table(name = "mouvements_stock")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class MouvementStockJpaEntity extends BaseJpaEntity {

    @Column(name = "type_mouvement", nullable = false, length = 30)
    private String typeMouvement;

    @Column(name = "sens", nullable = false, length = 10)
    private String sens;

    @Column(name = "entrepot_source_id")
    private UUID entrepotSourceId;

    @Column(name = "entrepot_destination_id")
    private UUID entrepotDestinationId;

    @Column(name = "commande_id")
    private UUID commandeId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "medicament_id", nullable = false)
    private UUID medicamentId;

    @Column(name = "quantite", nullable = false, precision = 14, scale = 4)
    private BigDecimal quantite;

    @Column(name = "date_mouvement", nullable = false)
    private Instant dateMouvement;

    @Column(name = "reference_document", length = 100)
    private String referenceDocument;

    @Column(name = "motif", length = 255)
    private String motif;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    public MouvementStockJpaEntity(UUID id, String typeMouvement, String sens, UUID entrepotSourceId,
            UUID entrepotDestinationId, UUID commandeId, UUID lotId, UUID medicamentId, BigDecimal quantite,
            Instant dateMouvement, String referenceDocument, String motif, UUID utilisateurId) {
        super(id);
        this.typeMouvement = typeMouvement;
        this.sens = sens;
        this.entrepotSourceId = entrepotSourceId;
        this.entrepotDestinationId = entrepotDestinationId;
        this.commandeId = commandeId;
        this.lotId = lotId;
        this.medicamentId = medicamentId;
        this.quantite = quantite;
        this.dateMouvement = dateMouvement;
        this.referenceDocument = referenceDocument;
        this.motif = motif;
        this.utilisateurId = utilisateurId;
    }
}
