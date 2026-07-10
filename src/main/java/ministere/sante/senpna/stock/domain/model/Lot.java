package ministere.sante.senpna.stock.domain.model;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;
import ministere.sante.senpna.stock.domain.exception.lot.LotExpireException;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Lot — unité de traçabilité d'un médicament reçu.
 *
 *
 * <p>
 * Invariants portés par ce modèle :
 * </p>
 * <ul>
 * <li>tout lot possède obligatoirement une date d'expiration ;</li>
 * <li>si la date de fabrication est renseignée, elle précède (ou est
 * égale à) la date d'expiration ;</li>
 * <li>un lot {@code EXPIRE} ou {@code BLOQUE} ne peut plus être réservé ni
 * expédié — {@link #peutEtreReserveOuExpedie()} centralise cette
 * règle, appliquée par le module {@code stock} avant toute réservation
 * ou sortie.</li>
 * </ul>
 *
 * <p>
 * L'unicité du numéro de lot pour un médicament donné est un invariant
 * <em>inter-agrégats</em> — vérifié par les use cases via le port de
 * persistance, pas par ce modèle qui n'a connaissance que de lui-même.
 * </p>
 */
public class Lot extends AggregateRoot<LotId> {

    private static final int NUMERO_LOT_MAX_LENGTH = 50;

    private String numeroLot;
    private MedicamentId medicamentId;
    private FournisseurId fournisseurId;
    private LocalDate dateFabrication;
    private LocalDate dateExpiration;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private StatutLot statut;

    private Lot(LotId id, String numeroLot, MedicamentId medicamentId, FournisseurId fournisseurId,
            LocalDate dateFabrication, LocalDate dateExpiration, BigDecimal prixAchat, BigDecimal prixVente,
            StatutLot statut, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.numeroLot = validerNumeroLot(numeroLot);
        this.medicamentId = Objects.requireNonNull(medicamentId, "Le médicament est obligatoire");
        this.fournisseurId = Objects.requireNonNull(fournisseurId, "Le fournisseur est obligatoire");
        this.dateExpiration = Objects.requireNonNull(dateExpiration, "La date d'expiration est obligatoire");
        this.dateFabrication = validerDateFabrication(dateFabrication, this.dateExpiration);
        this.prixAchat = validerPrix(prixAchat, "Le prix d'achat");
        this.prixVente = validerPrix(prixVente, "Le prix de vente");
        this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    }

    public static Lot reconstruct(LotId id, String numeroLot, MedicamentId medicamentId, FournisseurId fournisseurId,
            LocalDate dateFabrication, LocalDate dateExpiration, BigDecimal prixAchat, BigDecimal prixVente,
            StatutLot statut, Instant createdAt, Instant updatedAt) {
        return new Lot(id, numeroLot, medicamentId, fournisseurId, dateFabrication, dateExpiration, prixAchat,
                prixVente, statut, createdAt, updatedAt);
    }

    public static Lot creer(String numeroLot, MedicamentId medicamentId, FournisseurId fournisseurId,
            LocalDate dateFabrication, LocalDate dateExpiration, BigDecimal prixAchat, BigDecimal prixVente) {
        Instant maintenant = Instant.now();
        return new Lot(LotId.generate(), numeroLot, medicamentId, fournisseurId, dateFabrication, dateExpiration,
                prixAchat, prixVente, StatutLot.ACTIF, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    /**
     * Un lot ne peut être réservé ou expédié que s'il est {@code ACTIF} et
     * non expiré (cf. modèle métier complémentaire §2 « Lots » : « Les lots
     * expirés ne peuvent pas être expédiés / réservés »).
     */
    public boolean peutEtreReserveOuExpedie() {
        return statut == StatutLot.ACTIF && !estExpire();
    }

    public boolean estExpire() {
        return dateExpiration.isBefore(LocalDate.now());
    }

    /**
     * Nombre de jours calendaires restant avant expiration (négatif si déjà
     * expiré) — utilisé pour le calcul des alertes de péremption (12/6/3/1
     * mois, cf. doc. métier §17).
     */
    public long joursAvantExpiration() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dateExpiration);
    }

    public void bloquer() {
        if (statut == StatutLot.EXPIRE) {
            throw new LotExpireException(numeroLot);
        }
        if (statut == StatutLot.BLOQUE) {
            return;
        }
        this.statut = StatutLot.BLOQUE;
        markUpdated();
    }

    public void debloquer() {
        if (statut == StatutLot.EXPIRE) {
            throw new LotExpireException(numeroLot);
        }
        if (statut == StatutLot.ACTIF) {
            return;
        }
        this.statut = StatutLot.ACTIF;
        markUpdated();
    }

    /**
     * Positionne le lot comme expiré — invoqué par le job planifié qui
     * détecte le franchissement de la date d'expiration (cf.
     * {@code MarquerLotsExpiresUseCase}). Statut terminal : idempotent.
     */
    public void marquerExpire() {
        if (statut == StatutLot.EXPIRE) {
            return;
        }
        this.statut = StatutLot.EXPIRE;
        markUpdated();
    }

    public void modifierPrix(BigDecimal prixAchat, BigDecimal prixVente) {
        this.prixAchat = validerPrix(prixAchat, "Le prix d'achat");
        this.prixVente = validerPrix(prixVente, "Le prix de vente");
        markUpdated();
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerNumeroLot(String numeroLot) {
        Objects.requireNonNull(numeroLot, "Le numéro de lot ne peut pas être null");
        String trimmed = numeroLot.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le numéro de lot ne peut pas être vide");
        }
        if (trimmed.length() > NUMERO_LOT_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le numéro de lot ne peut pas dépasser " + NUMERO_LOT_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static LocalDate validerDateFabrication(LocalDate dateFabrication, LocalDate dateExpiration) {
        if (dateFabrication != null && dateFabrication.isAfter(dateExpiration)) {
            throw new IllegalArgumentException(
                    "La date de fabrication ne peut pas être postérieure à la date d'expiration");
        }
        return dateFabrication;
    }

    private static BigDecimal validerPrix(BigDecimal prix, String libelle) {
        if (prix != null && prix.signum() < 0) {
            throw new IllegalArgumentException(libelle + " ne peut pas être négatif");
        }
        return prix;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getNumeroLot() {
        return numeroLot;
    }

    public MedicamentId getMedicamentId() {
        return medicamentId;
    }

    public FournisseurId getFournisseurId() {
        return fournisseurId;
    }

    public LocalDate getDateFabrication() {
        return dateFabrication;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }

    public BigDecimal getPrixVente() {
        return prixVente;
    }

    public StatutLot getStatut() {
        return statut;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(numeroLot);
        result = prime * result + Objects.hashCode(medicamentId);
        result = prime * result + Objects.hashCode(fournisseurId);
        result = prime * result + Objects.hashCode(dateExpiration);
        result = prime * result + Objects.hashCode(statut);
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
        Lot other = (Lot) obj;
        return Objects.equals(numeroLot, other.numeroLot)
                && Objects.equals(medicamentId, other.medicamentId)
                && Objects.equals(fournisseurId, other.fournisseurId)
                && Objects.equals(dateFabrication, other.dateFabrication)
                && Objects.equals(dateExpiration, other.dateExpiration)
                && Objects.equals(prixAchat, other.prixAchat)
                && Objects.equals(prixVente, other.prixVente)
                && statut == other.statut;
    }
}
