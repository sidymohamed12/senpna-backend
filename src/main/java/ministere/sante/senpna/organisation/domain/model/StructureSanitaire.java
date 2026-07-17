package ministere.sante.senpna.organisation.domain.model;

import ministere.sante.senpna.organisation.domain.exception.DemandeAdhesionDejaTraiteeException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;
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
    private String responsableNom;
    private String responsablePrenom;
    private StatutAdhesion statutAdhesion;
    private String motifRejet;
    private boolean actif;

    private StructureSanitaire(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.code = validerCode(builder.code);
        this.nom = validerNom(builder.nom);
        this.type = Objects.requireNonNull(builder.type, "Le type de structure sanitaire ne peut pas être null");
        this.regionId = builder.regionId;
        this.praId = builder.praId;
        this.district = builder.district;
        this.adresse = builder.adresse;
        this.telephone = builder.telephone;
        this.email = builder.email;
        this.responsableNom = builder.responsableNom;
        this.responsablePrenom = builder.responsablePrenom;
        this.statutAdhesion = Objects.requireNonNull(builder.statutAdhesion,
                "Le statut d'adhésion ne peut pas être null");
        this.motifRejet = builder.motifRejet;
        this.actif = builder.actif;
    }

    /** Données nécessaires à la création d'une nouvelle demande d'adhésion. */
    public record CreationCommand(String code, String nom, TypeStructureSanitaire type, RegionId regionId,
            String district, String adresse, String telephone, String email, String responsableNom,
            String responsablePrenom) {
    }

    /**
     * Enregistre une nouvelle demande d'adhésion, rattachée dès sa
     * création à une région. La structure n'est ni rattachée à une PRA
     * précise, ni active tant que la demande n'a pas été validée
     * (cf. {@link #validerAdhesion()}).
     */
    public static StructureSanitaire creer(CreationCommand command) {
        Objects.requireNonNull(command.regionId(), "La région est obligatoire pour une demande d'adhésion");
        Objects.requireNonNull(command.responsableNom(), "Le nom du responsable est obligatoire");
        Objects.requireNonNull(command.responsablePrenom(), "Le prénom du responsable est obligatoire");
        Instant maintenant = Instant.now();
        return builder()
                .id(StructureSanitaireId.generate())
                .code(command.code())
                .nom(command.nom())
                .type(command.type())
                .regionId(command.regionId())
                .district(command.district())
                .adresse(command.adresse())
                .telephone(command.telephone())
                .email(command.email())
                .responsableNom(command.responsableNom())
                .responsablePrenom(command.responsablePrenom())
                .statutAdhesion(StatutAdhesion.EN_ATTENTE_VALIDATION)
                .actif(false)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private StructureSanitaireId id;
        private String code;
        private String nom;
        private TypeStructureSanitaire type;
        private RegionId regionId;
        private EntrepotId praId;
        private String district;
        private String adresse;
        private String telephone;
        private String email;
        private String responsableNom;
        private String responsablePrenom;
        private StatutAdhesion statutAdhesion;
        private String motifRejet;
        private boolean actif;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(StructureSanitaireId id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder type(TypeStructureSanitaire type) {
            this.type = type;
            return this;
        }

        public Builder regionId(RegionId regionId) {
            this.regionId = regionId;
            return this;
        }

        public Builder praId(EntrepotId praId) {
            this.praId = praId;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder adresse(String adresse) {
            this.adresse = adresse;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder responsableNom(String responsableNom) {
            this.responsableNom = responsableNom;
            return this;
        }

        public Builder responsablePrenom(String responsablePrenom) {
            this.responsablePrenom = responsablePrenom;
            return this;
        }

        public Builder statutAdhesion(StatutAdhesion statutAdhesion) {
            this.statutAdhesion = statutAdhesion;
            return this;
        }

        public Builder motifRejet(String motifRejet) {
            this.motifRejet = motifRejet;
            return this;
        }

        public Builder actif(boolean actif) {
            this.actif = actif;
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

        public StructureSanitaire build() {
            return new StructureSanitaire(this);
        }
    }

    // ── Cycle de vie de l'adhésion ──────────────────────────────────────

    public void validerAdhesion() {
        exigerAdhesionEnAttente();
        this.statutAdhesion = StatutAdhesion.VALIDEE;
        this.motifRejet = null;
        this.actif = true;
        markUpdated();
        registerEvent(new AdhesionValideeEvent(
                getId().getValue(), nom, responsableNom, responsablePrenom, email, Instant.now()));
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
            String responsableNom, String responsablePrenom) {
        this.nom = validerNom(nom);
        this.district = district;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.responsableNom = responsableNom;
        this.responsablePrenom = responsablePrenom;
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

    public String getResponsableNom() {
        return responsableNom;
    }

    public String getResponsablePrenom() {
        return responsablePrenom;
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
