package ministere.sante.senpna.organisation.domain.model;

import ministere.sante.senpna.organisation.domain.exception.DemandeAdhesionDejaTraiteeException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Établissement de santé bénéficiaire des médicaments (hôpital, district
 * sanitaire, centre de santé, poste de santé, ONG).
 *
 * <h3>Cycle de vie de l'adhésion</h3>
 *
 * <pre>
 * creer()  ──►  EN_ATTENTE_VALIDATION (actif = false)
 *                       │
 *          ┌────────────┴────────────┐
 *   validerAdhesion()          rejeterAdhesion(motif)
 *          │                          │
 *          ▼                          ▼
 *       VALIDEE                    REJETEE
 *   (actif = true)
 * </pre>
 *
 * <p>
 * La région est obligatoirement renseignée dès la création de la demande
 * d'adhésion (l'établissement sait toujours de quelle région il dépend).
 * Le rattachement à une PRA précise ({@link Entrepot}), en revanche, n'est
 * réalisé que via {@link #affecterPra(EntrepotId)} — typiquement au moment
 * de la validation de l'adhésion, une fois la PRA responsable désignée.
 * La région peut être corrigée a posteriori via
 * {@link #affecterRegion(RegionId)} si nécessaire.
 * </p>
 *
 * <p>
 * Une structure ne peut être (ré)activée que si son adhésion a été validée
 * — cf. {@link #activer()}.
 * </p>
 */
public class StructureSanitaire extends AggregateRoot<StructureSanitaireId> {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9\\-]{2,30}$");
    private static final int NOM_MAX_LENGTH = 150;

    private String code;
    private String nom;
    private final TypeStructureSanitaire type;
    private RegionId regionId;
    private EntrepotId praId;
    private String district;
    private String adresse;
    private String telephone;
    private String email;
    private String responsable;
    private StatutAdhesion statutAdhesion;
    private String motifRejet;
    private boolean actif;

    private StructureSanitaire(StructureSanitaireId id, String code, String nom, TypeStructureSanitaire type,
            RegionId regionId, EntrepotId praId, String district, String adresse, String telephone, String email,
            String responsable, StatutAdhesion statutAdhesion, String motifRejet, boolean actif, Instant createdAt,
            Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.code = validerCode(code);
        this.nom = validerNom(nom);
        this.type = Objects.requireNonNull(type, "Le type de structure sanitaire ne peut pas être null");
        this.regionId = regionId;
        this.praId = praId;
        this.district = district;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.responsable = responsable;
        this.statutAdhesion = Objects.requireNonNull(statutAdhesion, "Le statut d'adhésion ne peut pas être null");
        this.motifRejet = motifRejet;
        this.actif = actif;
    }

    public static StructureSanitaire reconstruct(StructureSanitaireId id, String code, String nom,
            TypeStructureSanitaire type, RegionId regionId, EntrepotId praId, String district, String adresse,
            String telephone, String email, String responsable, StatutAdhesion statutAdhesion, String motifRejet,
            boolean actif, Instant createdAt, Instant updatedAt) {
        return new StructureSanitaire(id, code, nom, type, regionId, praId, district, adresse, telephone, email,
                responsable, statutAdhesion, motifRejet, actif, createdAt, updatedAt);
    }

    /**
     * Enregistre une nouvelle demande d'adhésion, rattachée dès sa
     * création à une région. La structure n'est ni rattachée à une PRA
     * précise, ni active tant que la demande n'a pas été validée
     * (cf. {@link #validerAdhesion()}).
     */
    public static StructureSanitaire creer(String code, String nom, TypeStructureSanitaire type, RegionId regionId,
            String district, String adresse, String telephone, String email, String responsable) {
        Objects.requireNonNull(regionId, "La région est obligatoire pour une demande d'adhésion");
        Instant maintenant = Instant.now();
        return new StructureSanitaire(StructureSanitaireId.generate(), code, nom, type, regionId, null, district,
                adresse, telephone, email, responsable, StatutAdhesion.EN_ATTENTE_VALIDATION, null, false,
                maintenant, maintenant);
    }

    // ── Cycle de vie de l'adhésion ──────────────────────────────────────

    public void validerAdhesion() {
        exigerAdhesionEnAttente();
        this.statutAdhesion = StatutAdhesion.VALIDEE;
        this.motifRejet = null;
        this.actif = true;
        markUpdated();
    }

    public void rejeterAdhesion(String motif) {
        exigerAdhesionEnAttente();
        this.statutAdhesion = StatutAdhesion.REJETEE;
        this.motifRejet = motif;
        this.actif = false;
        markUpdated();
    }

    private void exigerAdhesionEnAttente() {
        if (this.statutAdhesion != StatutAdhesion.EN_ATTENTE_VALIDATION) {
            throw new DemandeAdhesionDejaTraiteeException();
        }
    }

    // ── Affectations organisationnelles ─────────────────────────────────

    public void affecterRegion(RegionId regionId) {
        this.regionId = Objects.requireNonNull(regionId, "La région ne peut pas être null");
        markUpdated();
    }

    public void affecterPra(EntrepotId praId) {
        this.praId = Objects.requireNonNull(praId, "La PRA de rattachement ne peut pas être null");
        markUpdated();
    }

    // ── Informations générales ──────────────────────────────────────────

    public void modifierInformations(String nom, String district, String adresse, String telephone, String email,
            String responsable) {
        this.nom = validerNom(nom);
        this.district = district;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.responsable = responsable;
        markUpdated();
    }

    public void activer() {
        if (this.statutAdhesion != StatutAdhesion.VALIDEE) {
            throw new StructureSanitaireNonValideeException();
        }
        if (this.actif) {
            return;
        }
        this.actif = true;
        markUpdated();
    }

    public void desactiver() {
        if (!this.actif) {
            return;
        }
        this.actif = false;
        markUpdated();
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerCode(String code) {
        Objects.requireNonNull(code, "Le code de la structure sanitaire ne peut pas être null");
        String normalise = code.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(normalise).matches()) {
            throw new IllegalArgumentException(
                    "Le code de la structure sanitaire doit être alphanumérique (2 à 30 caractères) : " + code);
        }
        return normalise;
    }

    private static String validerNom(String nom) {
        Objects.requireNonNull(nom, "Le nom de la structure sanitaire ne peut pas être null");
        String trimmed = nom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom de la structure sanitaire ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException("Le nom de la structure sanitaire ne peut pas dépasser "
                    + NOM_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public TypeStructureSanitaire getType() {
        return type;
    }

    public RegionId getRegionId() {
        return regionId;
    }

    public EntrepotId getPraId() {
        return praId;
    }

    public String getDistrict() {
        return district;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getResponsable() {
        return responsable;
    }

    public StatutAdhesion getStatutAdhesion() {
        return statutAdhesion;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public boolean isActif() {
        return actif;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(code);
        result = prime * result + Objects.hashCode(nom);
        result = prime * result + Objects.hashCode(type);
        result = prime * result + Objects.hashCode(regionId);
        result = prime * result + Objects.hashCode(praId);
        result = prime * result + Objects.hashCode(statutAdhesion);
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
        StructureSanitaire other = (StructureSanitaire) obj;
        return actif == other.actif
                && Objects.equals(code, other.code)
                && Objects.equals(nom, other.nom)
                && type == other.type
                && Objects.equals(regionId, other.regionId)
                && Objects.equals(praId, other.praId)
                && statutAdhesion == other.statutAdhesion;
    }
}
