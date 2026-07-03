package ministere.sante.senpna.medicament.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Famille thérapeutique — référentiel national de classification des
 * médicaments (ex : ANTIBIOTIQUE, ANTALGIQUE, ANTIPALUDÉEN). Utilisée par
 * {@link Medicament} pour le classement et les statistiques de
 * consommation par famille.
 *
 */
public class Famille extends AggregateRoot<FamilleId> {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_-]{2,30}$");
    private static final int LIBELLE_MAX_LENGTH = 150;
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    private String code;
    private String libelle;
    private String description;
    private boolean actif;

    private Famille(FamilleId id, String code, String libelle, String description, boolean actif,
            Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.code = validerCode(code);
        this.libelle = validerLibelle(libelle);
        this.description = validerDescription(description);
        this.actif = actif;
    }

    public static Famille reconstruct(FamilleId id, String code, String libelle, String description, boolean actif,
            Instant createdAt, Instant updatedAt) {
        return new Famille(id, code, libelle, description, actif, createdAt, updatedAt);
    }

    public static Famille creer(String code, String libelle, String description) {
        Instant maintenant = Instant.now();
        return new Famille(FamilleId.generate(), code, libelle, description, true, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierInformations(String libelle, String description) {
        this.libelle = validerLibelle(libelle);
        this.description = validerDescription(description);
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

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerCode(String code) {
        Objects.requireNonNull(code, "Le code de la famille ne peut pas être null");
        String normalise = code.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(normalise).matches()) {
            throw new IllegalArgumentException(
                    "Le code de la famille doit être alphanumérique (2 à 30 caractères) : " + code);
        }
        return normalise;
    }

    private static String validerLibelle(String libelle) {
        Objects.requireNonNull(libelle, "Le libellé de la famille ne peut pas être null");
        String trimmed = libelle.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le libellé de la famille ne peut pas être vide");
        }
        if (trimmed.length() > LIBELLE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le libellé de la famille ne peut pas dépasser " + LIBELLE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "La description de la famille ne peut pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActif() {
        return actif;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(code);
        result = prime * result + Objects.hashCode(libelle);
        result = prime * result + Objects.hashCode(description);
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
        Famille other = (Famille) obj;
        return actif == other.actif && Objects.equals(code, other.code) && Objects.equals(libelle, other.libelle)
                && Objects.equals(description, other.description);
    }
}
