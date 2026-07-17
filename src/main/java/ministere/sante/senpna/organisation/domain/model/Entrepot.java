package ministere.sante.senpna.organisation.domain.model;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * <p>
 * Cette version du module {@code organisation} expose exclusivement le
 * cycle de vie des PRA (création, modification, activation/désactivation) —
 * cf. {@code CreatePraUseCase}, {@code UpdatePraUseCase},
 * {@code DeactivatePraUseCase}. La PNA centrale est provisionnée par
 * migration de données (donnée de référence unique, non gérée via API).
 * </p>
 */
public class Entrepot extends AggregateRoot<EntrepotId> {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9\\-]{2,30}$");
    private static final int NOM_MAX_LENGTH = 150;

    private String code;
    private String nom;
    private final TypeEntrepot type;
    private RegionId regionId;
    private String adresse;
    private String telephone;
    private UUID responsableUserId;
    private boolean actif;

    private Entrepot(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.code = validerCode(builder.code);
        this.nom = validerNom(builder.nom);
        this.type = Objects.requireNonNull(builder.type, "Le type d'entrepôt ne peut pas être null");
        this.regionId = exigerRegionSiPra(builder.type, builder.regionId);
        this.adresse = builder.adresse;
        this.telephone = builder.telephone;
        this.responsableUserId = builder.responsableUserId;
        this.actif = builder.actif;
    }

    /** Données nécessaires à la création d'une nouvelle PRA. */
    public record CreationCommand(String code, String nom, RegionId regionId, String adresse, String telephone) {
    }

    public static Entrepot creerPra(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(EntrepotId.generate())
                .code(command.code())
                .nom(command.nom())
                .type(TypeEntrepot.PRA)
                .regionId(command.regionId())
                .adresse(command.adresse())
                .telephone(command.telephone())
                .actif(true)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private EntrepotId id;
        private String code;
        private String nom;
        private TypeEntrepot type;
        private RegionId regionId;
        private String adresse;
        private String telephone;
        private UUID responsableUserId;
        private boolean actif;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(EntrepotId id) {
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

        public Builder type(TypeEntrepot type) {
            this.type = type;
            return this;
        }

        public Builder regionId(RegionId regionId) {
            this.regionId = regionId;
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

        public Builder responsableUserId(UUID responsableUserId) {
            this.responsableUserId = responsableUserId;
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

        public Entrepot build() {
            return new Entrepot(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierInformations(String nom, String adresse, String telephone, RegionId regionId) {
        this.nom = validerNom(nom);
        this.adresse = adresse;
        this.telephone = telephone;
        this.regionId = exigerRegionSiPra(this.type, regionId != null ? regionId : this.regionId);
        markUpdated();
    }

    public void affecterResponsable(UUID responsableUserId) {
        this.responsableUserId = responsableUserId;
        markUpdated();
    }

    public void activer() {
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

    public boolean estPra() {
        return this.type == TypeEntrepot.PRA;
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static RegionId exigerRegionSiPra(TypeEntrepot type, RegionId regionId) {
        if (type == TypeEntrepot.PRA && regionId == null) {
            throw new IllegalArgumentException("Une PRA doit obligatoirement être rattachée à une région");
        }
        return regionId;
    }

    private static String validerCode(String code) {
        Objects.requireNonNull(code, "Le code de l'entrepôt ne peut pas être null");
        String normalise = code.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(normalise).matches()) {
            throw new IllegalArgumentException(
                    "Le code de l'entrepôt doit être alphanumérique (2 à 30 caractères) : " + code);
        }
        return normalise;
    }

    private static String validerNom(String nom) {
        Objects.requireNonNull(nom, "Le nom de l'entrepôt ne peut pas être null");
        String trimmed = nom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'entrepôt ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException("Le nom de l'entrepôt ne peut pas dépasser " + NOM_MAX_LENGTH
                    + " caractères");
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

    public TypeEntrepot getType() {
        return type;
    }

    public RegionId getRegionId() {
        return regionId;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public UUID getResponsableUserId() {
        return responsableUserId;
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
        result = prime * result + Objects.hashCode(adresse);
        result = prime * result + Objects.hashCode(telephone);
        result = prime * result + Objects.hashCode(responsableUserId);
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
        Entrepot other = (Entrepot) obj;
        return actif == other.actif
                && Objects.equals(code, other.code)
                && Objects.equals(nom, other.nom)
                && type == other.type
                && Objects.equals(regionId, other.regionId)
                && Objects.equals(adresse, other.adresse)
                && Objects.equals(telephone, other.telephone)
                && Objects.equals(responsableUserId, other.responsableUserId);
    }
}
