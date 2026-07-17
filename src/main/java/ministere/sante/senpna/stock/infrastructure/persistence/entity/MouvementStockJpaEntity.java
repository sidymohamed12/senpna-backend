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

    private MouvementStockJpaEntity(Builder builder) {
        super(builder.id);
        this.typeMouvement = builder.typeMouvement;
        this.sens = builder.sens;
        this.entrepotSourceId = builder.entrepotSourceId;
        this.entrepotDestinationId = builder.entrepotDestinationId;
        this.commandeId = builder.commandeId;
        this.lotId = builder.lotId;
        this.medicamentId = builder.medicamentId;
        this.quantite = builder.quantite;
        this.dateMouvement = builder.dateMouvement;
        this.referenceDocument = builder.referenceDocument;
        this.motif = builder.motif;
        this.utilisateurId = builder.utilisateurId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String typeMouvement;
        private String sens;
        private UUID entrepotSourceId;
        private UUID entrepotDestinationId;
        private UUID commandeId;
        private UUID lotId;
        private UUID medicamentId;
        private BigDecimal quantite;
        private Instant dateMouvement;
        private String referenceDocument;
        private String motif;
        private UUID utilisateurId;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder typeMouvement(String typeMouvement) {
            this.typeMouvement = typeMouvement;
            return this;
        }

        public Builder sens(String sens) {
            this.sens = sens;
            return this;
        }

        public Builder entrepotSourceId(UUID entrepotSourceId) {
            this.entrepotSourceId = entrepotSourceId;
            return this;
        }

        public Builder entrepotDestinationId(UUID entrepotDestinationId) {
            this.entrepotDestinationId = entrepotDestinationId;
            return this;
        }

        public Builder commandeId(UUID commandeId) {
            this.commandeId = commandeId;
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

        public Builder quantite(BigDecimal quantite) {
            this.quantite = quantite;
            return this;
        }

        public Builder dateMouvement(Instant dateMouvement) {
            this.dateMouvement = dateMouvement;
            return this;
        }

        public Builder referenceDocument(String referenceDocument) {
            this.referenceDocument = referenceDocument;
            return this;
        }

        public Builder motif(String motif) {
            this.motif = motif;
            return this;
        }

        public Builder utilisateurId(UUID utilisateurId) {
            this.utilisateurId = utilisateurId;
            return this;
        }

        public MouvementStockJpaEntity build() {
            return new MouvementStockJpaEntity(this);
        }
    }

}
