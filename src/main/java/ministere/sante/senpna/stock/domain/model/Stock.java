package ministere.sante.senpna.stock.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;
import ministere.sante.senpna.stock.domain.exception.ReservationInsuffisanteException;
import ministere.sante.senpna.stock.domain.exception.StockInsuffisantException;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Stock — ligne de stock d'un lot dans un entrepôt donné.
 *
 * <p>
 * Racine d'agrégat du module {@code stock}. Référence {@code Entrepot}
 * (module {@code organisation}) et {@link Lot} (même module, mais agrégat
 * distinct) uniquement par identifiant — jamais de relation JPA directe.
 * L'unicité de la ligne pour le couple (entrepôt, lot) est un invariant
 * <em>inter-agrégats</em> vérifié par les use cases via le port de
 * persistance.
 * </p>
 *
 * <h3>Sémantique des quantités</h3>
 * <p>
 * {@code quantiteDisponible} représente la quantité physiquement présente
 * dans l'entrepôt pour ce lot. {@code quantiteReservee} est la portion de
 * cette quantité déjà affectée à des commandes validées mais pas encore
 * expédiées — elle ne peut jamais dépasser {@code quantiteDisponible} (cf.
 * modèle métier complémentaire §2 « Stocks »). La quantité réellement
 * disponible <em>à la vente</em> (celle qui peut encore être réservée) est
 * donc {@link #getQuantiteDisponibleALaVente()} =
 * {@code quantiteDisponible - quantiteReservee}. {@code quantiteEnCommande}
 * est un compteur informatif du réapprovisionnement en cours (commandes
 * émises vers ce même entrepôt, pas encore réceptionnées) — il n'entre pas
 * dans le calcul de la disponibilité.
 * </p>
 *
 * <p>
 * Toute variation transite obligatoirement par une méthode de comportement
 * dédiée : aucune modification directe des quantités n'est exposée (cf.
 * modèle métier complémentaire §2 « Stocks » : « Aucune modification
 * directe du stock n'est autorisée »). Chaque appel correspond à un
 * {@code MouvementStock} distinct, créé par le use case appelant dans la
 * même transaction.
 * </p>
 */
public class Stock extends AggregateRoot<StockId> {

    private EntrepotId entrepotId;
    private LotId lotId;
    private MedicamentId medicamentId;
    private BigDecimal quantiteDisponible;
    private BigDecimal quantiteReservee;
    private BigDecimal quantiteEnCommande;
    private BigDecimal seuilAlerte;

    private Stock(StockId id, EntrepotId entrepotId, LotId lotId, MedicamentId medicamentId,
            BigDecimal quantiteDisponible, BigDecimal quantiteReservee, BigDecimal quantiteEnCommande,
            BigDecimal seuilAlerte, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.entrepotId = Objects.requireNonNull(entrepotId, "L'entrepôt est obligatoire");
        this.lotId = Objects.requireNonNull(lotId, "Le lot est obligatoire");
        this.medicamentId = Objects.requireNonNull(medicamentId, "Le médicament est obligatoire");
        this.quantiteDisponible = validerQuantitePositiveOuNulle(quantiteDisponible, "La quantité disponible");
        this.quantiteReservee = validerQuantitePositiveOuNulle(quantiteReservee, "La quantité réservée");
        this.quantiteEnCommande = validerQuantitePositiveOuNulle(quantiteEnCommande, "La quantité en commande");
        this.seuilAlerte = seuilAlerte;
        validerReserveeSousDisponible();
    }

    public static Stock reconstruct(StockId id, EntrepotId entrepotId, LotId lotId, MedicamentId medicamentId,
            BigDecimal quantiteDisponible, BigDecimal quantiteReservee, BigDecimal quantiteEnCommande,
            BigDecimal seuilAlerte, Instant createdAt, Instant updatedAt) {
        return new Stock(id, entrepotId, lotId, medicamentId, quantiteDisponible, quantiteReservee,
                quantiteEnCommande, seuilAlerte, createdAt, updatedAt);
    }

    /**
     * Ouvre une nouvelle ligne de stock à zéro pour le couple
     * (entrepôt, lot) — appelée par {@code EntreeStockUseCase} lorsqu'aucune
     * ligne n'existe encore pour ce lot dans cet entrepôt.
     */
    public static Stock ouvrir(EntrepotId entrepotId, LotId lotId, MedicamentId medicamentId,
            BigDecimal seuilAlerte) {
        Instant maintenant = Instant.now();
        return new Stock(StockId.generate(), entrepotId, lotId, medicamentId, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, seuilAlerte, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    /** Entrée en stock (achat, transfert reçu, retour, don, ajustement positif). */
    public void entrer(BigDecimal quantite) {
        BigDecimal montant = validerQuantitePositive(quantite);
        this.quantiteDisponible = this.quantiteDisponible.add(montant);
        markUpdated();
    }

    /**
     * Sortie physique de stock non adossée à une réservation préalable
     * (perte, casse, vol, péremption, ajustement négatif, don sortant). La
     * quantité déjà réservée reste protégée : la quantité disponible ne
     * peut pas descendre sous la quantité réservée.
     */
    public void sortir(BigDecimal quantite) {
        BigDecimal montant = validerQuantitePositive(quantite);
        BigDecimal disponibleALaVente = getQuantiteDisponibleALaVente();
        if (montant.compareTo(disponibleALaVente) > 0) {
            throw new StockInsuffisantException(disponibleALaVente, montant);
        }
        this.quantiteDisponible = this.quantiteDisponible.subtract(montant);
        markUpdated();
    }

    /**
     * Sortie physique consécutive à l'expédition d'une commande dont la
     * quantité avait préalablement été réservée : diminue à la fois la
     * quantité disponible et la quantité réservée (la réservation est
     * consommée).
     */
    public void sortirDepuisReservation(BigDecimal quantite) {
        BigDecimal montant = validerQuantitePositive(quantite);
        if (montant.compareTo(this.quantiteReservee) > 0) {
            throw new ReservationInsuffisanteException(this.quantiteReservee, montant);
        }
        this.quantiteDisponible = this.quantiteDisponible.subtract(montant);
        this.quantiteReservee = this.quantiteReservee.subtract(montant);
        markUpdated();
    }

    /**
     * Réserve automatiquement une quantité pour une commande validée (cf.
     * doc. métier §9 « Réservation automatique des quantités »). Échoue si
     * la quantité disponible à la vente est insuffisante.
     */
    public void reserver(BigDecimal quantite) {
        BigDecimal montant = validerQuantitePositive(quantite);
        BigDecimal disponibleALaVente = getQuantiteDisponibleALaVente();
        if (montant.compareTo(disponibleALaVente) > 0) {
            throw new StockInsuffisantException(disponibleALaVente, montant);
        }
        this.quantiteReservee = this.quantiteReservee.add(montant);
        markUpdated();
    }

    /**
     * Libère tout ou partie d'une réservation (commande annulée, rejetée ou
     * dont la quantité a été revue à la baisse).
     */
    public void libererReservation(BigDecimal quantite) {
        BigDecimal montant = validerQuantitePositive(quantite);
        if (montant.compareTo(this.quantiteReservee) > 0) {
            throw new ReservationInsuffisanteException(this.quantiteReservee, montant);
        }
        this.quantiteReservee = this.quantiteReservee.subtract(montant);
        markUpdated();
    }

    public void ajusterQuantiteEnCommande(BigDecimal delta) {
        Objects.requireNonNull(delta, "Le delta ne peut pas être null");
        BigDecimal nouvelleValeur = this.quantiteEnCommande.add(delta);
        if (nouvelleValeur.signum() < 0) {
            throw new IllegalArgumentException("La quantité en commande ne peut pas devenir négative");
        }
        this.quantiteEnCommande = nouvelleValeur;
        markUpdated();
    }

    public void definirSeuilAlerte(BigDecimal seuilAlerte) {
        if (seuilAlerte != null && seuilAlerte.signum() < 0) {
            throw new IllegalArgumentException("Le seuil d'alerte ne peut pas être négatif");
        }
        this.seuilAlerte = seuilAlerte;
        markUpdated();
    }

    // ── Requêtes dérivées ────────────────────────────────────────────────

    public BigDecimal getQuantiteDisponibleALaVente() {
        return quantiteDisponible.subtract(quantiteReservee);
    }

    public boolean estEnRupture() {
        return getQuantiteDisponibleALaVente().signum() <= 0;
    }

    public boolean seuilAtteint() {
        return seuilAlerte != null && getQuantiteDisponibleALaVente().compareTo(seuilAlerte) <= 0;
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static BigDecimal validerQuantitePositive(BigDecimal quantite) {
        Objects.requireNonNull(quantite, "La quantité ne peut pas être null");
        if (quantite.signum() <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive");
        }
        return quantite;
    }

    private static BigDecimal validerQuantitePositiveOuNulle(BigDecimal quantite, String libelle) {
        Objects.requireNonNull(quantite, libelle + " ne peut pas être null");
        if (quantite.signum() < 0) {
            throw new IllegalArgumentException(libelle + " ne peut pas être négative");
        }
        return quantite;
    }

    private void validerReserveeSousDisponible() {
        if (this.quantiteReservee.compareTo(this.quantiteDisponible) > 0) {
            throw new IllegalArgumentException(
                    "La quantité réservée ne peut pas dépasser la quantité disponible");
        }
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public EntrepotId getEntrepotId() {
        return entrepotId;
    }

    public LotId getLotId() {
        return lotId;
    }

    public MedicamentId getMedicamentId() {
        return medicamentId;
    }

    public BigDecimal getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public BigDecimal getQuantiteReservee() {
        return quantiteReservee;
    }

    public BigDecimal getQuantiteEnCommande() {
        return quantiteEnCommande;
    }

    public BigDecimal getSeuilAlerte() {
        return seuilAlerte;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(entrepotId);
        result = prime * result + Objects.hashCode(lotId);
        result = prime * result + Objects.hashCode(medicamentId);
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
        Stock other = (Stock) obj;
        return Objects.equals(entrepotId, other.entrepotId)
                && Objects.equals(lotId, other.lotId)
                && Objects.equals(medicamentId, other.medicamentId)
                && Objects.equals(quantiteDisponible, other.quantiteDisponible)
                && Objects.equals(quantiteReservee, other.quantiteReservee)
                && Objects.equals(quantiteEnCommande, other.quantiteEnCommande);
    }
}
