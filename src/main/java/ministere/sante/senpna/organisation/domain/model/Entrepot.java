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

    private Entrepot(EntrepotId id, String code, String nom, TypeEntrepot type, RegionId regionId, String adresse,
            String telephone, UUID responsableUserId, boolean actif, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.code = validerCode(code);
        this.nom = validerNom(nom);
        this.type = Objects.requireNonNull(type, "Le type d'entrepôt ne peut pas être null");
        this.regionId = exigerRegionSiPra(type, regionId);
        this.adresse = adresse;
        this.telephone = telephone;
        this.responsableUserId = responsableUserId;
        this.actif = actif;
    }

    public static Entrepot reconstruct(EntrepotId id, String code, String nom, TypeEntrepot type, RegionId regionId,
            String adresse, String telephone, UUID responsableUserId, boolean actif, Instant createdAt,
            Instant updatedAt) {
        return new Entrepot(id, code, nom, type, regionId, adresse, telephone, responsableUserId, actif, createdAt,
                updatedAt);
    }

    public static Entrepot creerPra(String code, String nom, RegionId regionId, String adresse, String telephone) {
        Instant maintenant = Instant.now();
        return new Entrepot(EntrepotId.generate(), code, nom, TypeEntrepot.PRA, regionId, adresse, telephone, null,
                true, maintenant, maintenant);
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
