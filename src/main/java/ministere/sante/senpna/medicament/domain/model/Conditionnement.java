package ministere.sante.senpna.medicament.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Conditionnement — niveau d'emballage d'un médicament (ex : Carton, Boîte,
 * Plaquette, Comprimé).
 *
 * <p>
 * Chaque médicament possède exactement <strong>un</strong> conditionnement
 * marqué {@code estUniteBase} — celui dans lequel le stock réel est
 * conservé (ex : le Comprimé). Les autres niveaux expriment leur
 * équivalence en nombre d'unités de base via {@link #getQuantiteUniteBase()}
 * (ex : 1 carton = 1000 comprimés → {@code quantiteUniteBase = 1000}).
 * L'unicité de l'unité de base par médicament, ainsi que l'unicité du
 * niveau et du nom, sont des invariants <em>inter-agrégats</em> — vérifiés
 * par les use cases via le port de persistance, le modèle de domaine
 * n'ayant connaissance que de lui-même.
 * </p>
 *
 * <p>
 * <strong>Le prix se négocie par conditionnement, jamais par unité de
 * base</strong> : on ne vend pas au comprimé mais à la boîte, au carton...
 * {@code prixAchat}/{@code prixVente} sont donc portés ici plutôt que sur
 * {@code Lot} — un carton et une boîte du même médicament peuvent avoir des
 * prix différents alors qu'ils proviennent du même lot. Les deux champs
 * sont optionnels et solidaires : un conditionnement sans prix n'est
 * simplement pas proposé à la commande (cf. catalogue, qui ne liste que
 * les conditionnements ayant un prix défini) ; un conditionnement dont
 * l'usage n'est pas commercial (ex : l'unité de base, gardée pour le
 * comptage de stock) peut rester sans prix indéfiniment.
 * </p>
 */
public class Conditionnement extends AggregateRoot<ConditionnementId> {

    private static final int NOM_MAX_LENGTH = 100;

    private MedicamentId medicamentId;
    private String nom;
    private int niveau;
    private BigDecimal quantiteUniteBase;
    private boolean estUniteBase;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private boolean actif;

    private Conditionnement(ConditionnementId id, MedicamentId medicamentId, String nom, int niveau,
            BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat, BigDecimal prixVente,
            boolean actif, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.medicamentId = Objects.requireNonNull(medicamentId, "Le médicament est obligatoire");
        this.nom = validerNom(nom);
        this.niveau = validerNiveau(niveau);
        this.quantiteUniteBase = validerQuantite(quantiteUniteBase, estUniteBase);
        this.estUniteBase = estUniteBase;
        validerPrix(prixAchat, prixVente);
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.actif = actif;
    }

    public static Conditionnement reconstruct(ConditionnementId id, MedicamentId medicamentId, String nom,
            int niveau, BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat,
            BigDecimal prixVente, boolean actif, Instant createdAt, Instant updatedAt) {
        return new Conditionnement(id, medicamentId, nom, niveau, quantiteUniteBase, estUniteBase, prixAchat,
                prixVente, actif, createdAt, updatedAt);
    }

    public static Conditionnement creer(MedicamentId medicamentId, String nom, int niveau,
            BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat, BigDecimal prixVente) {
        Instant maintenant = Instant.now();
        return new Conditionnement(ConditionnementId.generate(), medicamentId, nom, niveau, quantiteUniteBase,
                estUniteBase, prixAchat, prixVente, true, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierInformations(String nom, int niveau, BigDecimal quantiteUniteBase, boolean estUniteBase,
            BigDecimal prixAchat, BigDecimal prixVente) {
        this.nom = validerNom(nom);
        this.niveau = validerNiveau(niveau);
        this.quantiteUniteBase = validerQuantite(quantiteUniteBase, estUniteBase);
        this.estUniteBase = estUniteBase;
        validerPrix(prixAchat, prixVente);
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        markUpdated();
    }

    public void archiver() {
        if (!this.actif) {
            return;
        }
        this.actif = false;
        markUpdated();
    }

    public void desarchiver() {
        if (this.actif) {
            return;
        }
        this.actif = true;
        markUpdated();
    }

    /**
     * Convertit une quantité exprimée dans ce conditionnement vers l'unité
     * de base du médicament (ex : 20 cartons × 1000 → 20 000 comprimés).
     * Utilisé par les futurs modules Commande / Réception / Stock.
     */
    public BigDecimal convertirVersUniteBase(BigDecimal quantiteDansCeConditionnement) {
        Objects.requireNonNull(quantiteDansCeConditionnement, "La quantité à convertir ne peut pas être null");
        if (quantiteDansCeConditionnement.signum() < 0) {
            throw new IllegalArgumentException("La quantité à convertir ne peut pas être négative");
        }
        return quantiteDansCeConditionnement.multiply(quantiteUniteBase);
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerNom(String nom) {
        Objects.requireNonNull(nom, "Le nom du conditionnement ne peut pas être null");
        String trimmed = nom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom du conditionnement ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le nom du conditionnement ne peut pas dépasser " + NOM_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static int validerNiveau(int niveau) {
        if (niveau < 1) {
            throw new IllegalArgumentException("Le niveau du conditionnement doit être supérieur ou égal à 1");
        }
        return niveau;
    }

    private static BigDecimal validerQuantite(BigDecimal quantiteUniteBase, boolean estUniteBase) {
        Objects.requireNonNull(quantiteUniteBase, "La quantité en unité de base ne peut pas être null");
        if (quantiteUniteBase.signum() <= 0) {
            throw new IllegalArgumentException("La quantité en unité de base doit être strictement positive");
        }
        if (estUniteBase && quantiteUniteBase.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException(
                    "Le conditionnement marqué comme unité de base doit avoir une quantité de 1");
        }
        return quantiteUniteBase;
    }

    private static void validerPrix(BigDecimal prixAchat, BigDecimal prixVente) {
        if (prixAchat == null && prixVente == null) {
            return;
        }
        if (prixAchat == null || prixVente == null) {
            throw new IllegalArgumentException(
                    "Le prix d'achat et le prix de vente doivent être définis ensemble, ou pas du tout");
        }
        if (prixAchat.signum() < 0 || prixVente.signum() < 0) {
            throw new IllegalArgumentException("Les prix ne peuvent pas être négatifs");
        }
        if (prixVente.compareTo(prixAchat) < 0) {
            throw new IllegalArgumentException("Le prix de vente ne peut pas être inférieur au prix d'achat");
        }
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public MedicamentId getMedicamentId() {
        return medicamentId;
    }

    public String getNom() {
        return nom;
    }

    public int getNiveau() {
        return niveau;
    }

    public BigDecimal getQuantiteUniteBase() {
        return quantiteUniteBase;
    }

    public boolean isEstUniteBase() {
        return estUniteBase;
    }

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }

    public BigDecimal getPrixVente() {
        return prixVente;
    }

    public boolean aUnPrix() {
        return prixVente != null;
    }

    public boolean isActif() {
        return actif;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(medicamentId);
        result = prime * result + Objects.hashCode(nom);
        result = prime * result + niveau;
        result = prime * result + Objects.hashCode(quantiteUniteBase);
        result = prime * result + (estUniteBase ? 1231 : 1237);
        result = prime * result + Objects.hashCode(prixAchat);
        result = prime * result + Objects.hashCode(prixVente);
        result = prime * result + (actif ? 1231 : 1237);
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
        Conditionnement other = (Conditionnement) obj;
        return niveau == other.niveau
                && estUniteBase == other.estUniteBase
                && actif == other.actif
                && Objects.equals(medicamentId, other.medicamentId)
                && Objects.equals(nom, other.nom)
                && Objects.equals(quantiteUniteBase, other.quantiteUniteBase)
                && Objects.equals(prixAchat, other.prixAchat)
                && Objects.equals(prixVente, other.prixVente);
    }
}
