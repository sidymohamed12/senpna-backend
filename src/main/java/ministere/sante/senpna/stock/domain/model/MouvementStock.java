package ministere.sante.senpna.stock.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;
import ministere.sante.senpna.stock.domain.exception.mouvement.MouvementStockInvalideException;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * MouvementStock — écriture immuable du journal des mouvements de stock.
 *
 * <p>
 * Racine d'agrégat du module {@code stock}, mais sans comportement de
 * modification : une fois créé, un mouvement n'est jamais mis à jour ni
 * supprimé — il constitue la source de vérité de la traçabilité.
 * Toute variation de {@link Stock} doit être accompagnée de
 * la création d'un {@code MouvementStock} correspondant, dans la même
 * transaction applicative.
 * </p>
 *
 * <p>
 * Référence {@code Entrepot} (module {@code organisation}), {@link Lot}
 * (même module) et, le cas échéant, une commande (module {@code commande},
 * pas encore implémenté) uniquement par identifiant.
 * </p>
 */
public class MouvementStock extends AggregateRoot<MouvementStockId> {

    private static final int REFERENCE_MAX_LENGTH = 100;
    private static final int MOTIF_MAX_LENGTH = 255;

    private TypeMouvement typeMouvement;
    private SensMouvement sens;
    private EntrepotId entrepotSourceId;
    private EntrepotId entrepotDestinationId;
    private UUID commandeId;
    private LotId lotId;
    private MedicamentId medicamentId;
    private BigDecimal quantite;
    private Instant dateMouvement;
    private String referenceDocument;
    private String motif;
    private UUID utilisateurId;

    private MouvementStock(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.typeMouvement = Objects.requireNonNull(builder.typeMouvement, "Le type de mouvement est obligatoire");
        this.sens = Objects.requireNonNull(builder.sens, "Le sens du mouvement est obligatoire");
        this.lotId = Objects.requireNonNull(builder.lotId, "Le lot est obligatoire");
        this.medicamentId = Objects.requireNonNull(builder.medicamentId, "Le médicament est obligatoire");
        this.quantite = validerQuantite(builder.quantite);
        this.utilisateurId = Objects.requireNonNull(builder.utilisateurId, "L'utilisateur responsable est obligatoire");
        this.dateMouvement = builder.dateMouvement != null ? builder.dateMouvement : Instant.now();
        this.entrepotSourceId = builder.entrepotSourceId;
        this.entrepotDestinationId = builder.entrepotDestinationId;
        this.commandeId = builder.commandeId;
        this.referenceDocument = validerLongueur(builder.referenceDocument, REFERENCE_MAX_LENGTH,
                "La référence document");
        this.motif = validerLongueur(builder.motif, MOTIF_MAX_LENGTH, "Le motif");
        validerEntrepots();
    }

    /** Données nécessaires à l'enregistrement d'un nouveau mouvement de stock. */
    public record CreationCommand(TypeMouvement typeMouvement, SensMouvement sens, EntrepotId entrepotSourceId,
            EntrepotId entrepotDestinationId, UUID commandeId, LotId lotId, MedicamentId medicamentId,
            BigDecimal quantite, String referenceDocument, String motif, UUID utilisateurId) {
    }

    /**
     * Enregistre un nouveau mouvement de stock. Les règles structurelles
     * suivantes sont vérifiées à la construction :
     * <ul>
     * <li>une sortie diminue le stock de l'entrepôt source → entrepôt
     * source obligatoire ;</li>
     * <li>une entrée augmente le stock de l'entrepôt destination →
     * entrepôt destination obligatoire ;</li>
     * <li>un transfert ({@code ENTREE_TRANSFERT} / {@code SORTIE_TRANSFERT})
     * exige à la fois un entrepôt source et un entrepôt destination.</li>
     * </ul>
     */
    public static MouvementStock creer(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(MouvementStockId.generate())
                .typeMouvement(command.typeMouvement())
                .sens(command.sens())
                .entrepotSourceId(command.entrepotSourceId())
                .entrepotDestinationId(command.entrepotDestinationId())
                .commandeId(command.commandeId())
                .lotId(command.lotId())
                .medicamentId(command.medicamentId())
                .quantite(command.quantite())
                .dateMouvement(maintenant)
                .referenceDocument(command.referenceDocument())
                .motif(command.motif())
                .utilisateurId(command.utilisateurId())
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private MouvementStockId id;
        private TypeMouvement typeMouvement;
        private SensMouvement sens;
        private EntrepotId entrepotSourceId;
        private EntrepotId entrepotDestinationId;
        private UUID commandeId;
        private LotId lotId;
        private MedicamentId medicamentId;
        private BigDecimal quantite;
        private Instant dateMouvement;
        private String referenceDocument;
        private String motif;
        private UUID utilisateurId;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(MouvementStockId id) {
            this.id = id;
            return this;
        }

        public Builder typeMouvement(TypeMouvement typeMouvement) {
            this.typeMouvement = typeMouvement;
            return this;
        }

        public Builder sens(SensMouvement sens) {
            this.sens = sens;
            return this;
        }

        public Builder entrepotSourceId(EntrepotId entrepotSourceId) {
            this.entrepotSourceId = entrepotSourceId;
            return this;
        }

        public Builder entrepotDestinationId(EntrepotId entrepotDestinationId) {
            this.entrepotDestinationId = entrepotDestinationId;
            return this;
        }

        public Builder commandeId(UUID commandeId) {
            this.commandeId = commandeId;
            return this;
        }

        public Builder lotId(LotId lotId) {
            this.lotId = lotId;
            return this;
        }

        public Builder medicamentId(MedicamentId medicamentId) {
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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public MouvementStock build() {
            return new MouvementStock(this);
        }
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static BigDecimal validerQuantite(BigDecimal quantite) {
        Objects.requireNonNull(quantite, "La quantité ne peut pas être null");
        if (quantite.signum() <= 0) {
            throw new IllegalArgumentException("La quantité du mouvement doit être strictement positive");
        }
        return quantite;
    }

    private static String validerLongueur(String valeur, int maxLength, String libelle) {
        if (valeur == null) {
            return null;
        }
        String trimmed = valeur.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(libelle + " ne peut pas dépasser " + maxLength + " caractères");
        }
        return trimmed;
    }

    private void validerEntrepots() {
        boolean estTransfert = typeMouvement == TypeMouvement.ENTREE_TRANSFERT
                || typeMouvement == TypeMouvement.SORTIE_TRANSFERT;

        if (estTransfert && (entrepotSourceId == null || entrepotDestinationId == null)) {
            throw new MouvementStockInvalideException(
                    "Un mouvement de transfert doit obligatoirement posséder un entrepôt source et un entrepôt destination");
        }
        if (sens == SensMouvement.SORTIE && entrepotSourceId == null) {
            throw new MouvementStockInvalideException("Un mouvement de sortie doit posséder un entrepôt source");
        }
        if (sens == SensMouvement.ENTREE && entrepotDestinationId == null) {
            throw new MouvementStockInvalideException("Un mouvement d'entrée doit posséder un entrepôt destination");
        }
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public TypeMouvement getTypeMouvement() {
        return typeMouvement;
    }

    public SensMouvement getSens() {
        return sens;
    }

    public EntrepotId getEntrepotSourceId() {
        return entrepotSourceId;
    }

    public EntrepotId getEntrepotDestinationId() {
        return entrepotDestinationId;
    }

    public UUID getCommandeId() {
        return commandeId;
    }

    public LotId getLotId() {
        return lotId;
    }

    public MedicamentId getMedicamentId() {
        return medicamentId;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public Instant getDateMouvement() {
        return dateMouvement;
    }

    public String getReferenceDocument() {
        return referenceDocument;
    }

    public String getMotif() {
        return motif;
    }

    public UUID getUtilisateurId() {
        return utilisateurId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(typeMouvement);
        result = prime * result + Objects.hashCode(sens);
        result = prime * result + Objects.hashCode(lotId);
        result = prime * result + Objects.hashCode(quantite);
        result = prime * result + Objects.hashCode(dateMouvement);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        MouvementStock other = (MouvementStock) obj;
        return typeMouvement == other.typeMouvement
                && sens == other.sens
                && Objects.equals(entrepotSourceId, other.entrepotSourceId)
                && Objects.equals(entrepotDestinationId, other.entrepotDestinationId)
                && Objects.equals(commandeId, other.commandeId)
                && Objects.equals(lotId, other.lotId)
                && Objects.equals(medicamentId, other.medicamentId)
                && Objects.equals(quantite, other.quantite)
                && Objects.equals(dateMouvement, other.dateMouvement)
                && Objects.equals(utilisateurId, other.utilisateurId);
    }
}
