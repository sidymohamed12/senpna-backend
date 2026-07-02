package ministere.sante.senpna.organisation.domain.model;

import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

public class Region extends AggregateRoot<RegionId> {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_-]{2,20}$");
    private static final int NOM_MAX_LENGTH = 100;

    private String code;
    private String nom;
    private boolean actif;

    private Region(RegionId id, String code, String nom, boolean actif, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        Objects.requireNonNull(code, "Le code de la région ne peut pas être null");
        this.code = code.trim().toUpperCase();
        this.nom = validerNom(nom);
        this.actif = actif;
    }

    public static Region reconstruct(RegionId id, String code, String nom, boolean actif, Instant createdAt,
            Instant updatedAt) {
        return new Region(id, code, nom, actif, createdAt, updatedAt);
    }

    public static Region creer(String code, String nom) {
        String normalise = validerCode(code);
        Instant maintenant = Instant.now();
        return new Region(RegionId.generate(), normalise, nom, true, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void renommer(String nouveauNom) {
        this.nom = validerNom(nouveauNom);
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

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerCode(String code) {
        Objects.requireNonNull(code, "Le code de la région ne peut pas être null");
        String normalise = code.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(normalise).matches()) {
            throw new IllegalArgumentException(
                    "Le code de la région doit être alphanumérique (2 à 20 caractères) : " + code);
        }
        return normalise;
    }

    private static String validerNom(String nom) {
        Objects.requireNonNull(nom, "Le nom de la région ne peut pas être null");
        String trimmed = nom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom de la région ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException("Le nom de la région ne peut pas dépasser " + NOM_MAX_LENGTH
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

    public boolean isActif() {
        return actif;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(code);
        result = prime * result + Objects.hashCode(nom);
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
        Region other = (Region) obj;
        return actif == other.actif && Objects.equals(code, other.code) && Objects.equals(nom, other.nom);
    }
}
