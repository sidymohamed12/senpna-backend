package ministere.sante.senpna.medicament.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Médicament — référentiel national des médicaments.
 *
 * <p>
 * Racine d'agrégat du module {@code medicament}. Référence
 * {@link Famille} et {@link Forme} uniquement par identifiant (pas de
 * relation JPA directe) — chaque agrégat reste indépendamment
 * persistable/chargeable, conformément à la règle « un agrégat = une
 * transaction » de la Clean Architecture / DDD. L'existence et le
 * caractère actif de la famille et de la forme référencées sont vérifiés
 * par les use cases (couche application), pas par le modèle de domaine
 * lui-même, qui n'a pas accès aux autres agrégats.
 * </p>
 *
 */
public class Medicament extends AggregateRoot<MedicamentId> {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_-]{2,30}$");
    private static final int NOM_MAX_LENGTH = 200;
    private static final int DCI_MAX_LENGTH = 200;
    private static final int DOSAGE_MAX_LENGTH = 50;
    private static final int PROGRAMME_SANTE_MAX_LENGTH = 150;
    private static final int FABRICANT_MAX_LENGTH = 150;

    private String code;
    private String nomCommercial;
    private String dci;
    private String dosage;
    private FormeId formeId;
    private FamilleId familleId;
    private VoieAdministration voieAdministration;
    private TemperatureConservation temperatureConservation;
    private String programmeSante;
    private Integer delaiApprovisionnementJours;
    private boolean necessiteOrdonnance;
    private String fabricant;
    private Integer stockMinimum;
    private Integer stockMaximum;
    private boolean actif;

    private Medicament(MedicamentId id, String code, String nomCommercial, String dci, String dosage,
            FormeId formeId, FamilleId familleId, VoieAdministration voieAdministration,
            TemperatureConservation temperatureConservation, String programmeSante,
            Integer delaiApprovisionnementJours, boolean necessiteOrdonnance, String fabricant,
            Integer stockMinimum, Integer stockMaximum, boolean actif, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.code = validerCode(code);
        this.nomCommercial = validerNomCommercial(nomCommercial);
        this.dci = validerDci(dci);
        this.dosage = validerDosage(dosage);
        this.formeId = Objects.requireNonNull(formeId, "La forme pharmaceutique est obligatoire");
        this.familleId = Objects.requireNonNull(familleId, "La famille thérapeutique est obligatoire");
        this.voieAdministration = voieAdministration;
        this.temperatureConservation = temperatureConservation != null ? temperatureConservation
                : TemperatureConservation.AMBIANTE;
        this.programmeSante = validerProgrammeSante(programmeSante);
        this.delaiApprovisionnementJours = validerDelai(delaiApprovisionnementJours);
        this.necessiteOrdonnance = necessiteOrdonnance;
        this.fabricant = validerFabricant(fabricant);
        validerSeuils(stockMinimum, stockMaximum);
        this.stockMinimum = stockMinimum;
        this.stockMaximum = stockMaximum;
        this.actif = actif;
    }

    public static Medicament reconstruct(MedicamentId id, String code, String nomCommercial, String dci,
            String dosage, FormeId formeId, FamilleId familleId, VoieAdministration voieAdministration,
            TemperatureConservation temperatureConservation, String programmeSante,
            Integer delaiApprovisionnementJours, boolean necessiteOrdonnance, String fabricant,
            Integer stockMinimum, Integer stockMaximum, boolean actif, Instant createdAt, Instant updatedAt) {
        return new Medicament(id, code, nomCommercial, dci, dosage, formeId, familleId, voieAdministration,
                temperatureConservation, programmeSante, delaiApprovisionnementJours, necessiteOrdonnance, fabricant,
                stockMinimum, stockMaximum, actif, createdAt, updatedAt);
    }

    public static Medicament creer(String code, String nomCommercial, String dci, String dosage, FormeId formeId,
            FamilleId familleId, VoieAdministration voieAdministration,
            TemperatureConservation temperatureConservation, String programmeSante,
            Integer delaiApprovisionnementJours, boolean necessiteOrdonnance, String fabricant,
            Integer stockMinimum, Integer stockMaximum) {
        Instant maintenant = Instant.now();
        return new Medicament(MedicamentId.generate(), code, nomCommercial, dci, dosage, formeId, familleId,
                voieAdministration, temperatureConservation, programmeSante, delaiApprovisionnementJours,
                necessiteOrdonnance, fabricant, stockMinimum, stockMaximum, true, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierInformations(String nomCommercial, String dci, String dosage, FormeId formeId,
            FamilleId familleId, VoieAdministration voieAdministration,
            TemperatureConservation temperatureConservation, String programmeSante,
            Integer delaiApprovisionnementJours, boolean necessiteOrdonnance, String fabricant,
            Integer stockMinimum, Integer stockMaximum) {
        this.nomCommercial = validerNomCommercial(nomCommercial);
        this.dci = validerDci(dci);
        this.dosage = validerDosage(dosage);
        this.formeId = Objects.requireNonNull(formeId, "La forme pharmaceutique est obligatoire");
        this.familleId = Objects.requireNonNull(familleId, "La famille thérapeutique est obligatoire");
        this.voieAdministration = voieAdministration;
        this.temperatureConservation = temperatureConservation != null ? temperatureConservation
                : TemperatureConservation.AMBIANTE;
        this.programmeSante = validerProgrammeSante(programmeSante);
        this.delaiApprovisionnementJours = validerDelai(delaiApprovisionnementJours);
        this.necessiteOrdonnance = necessiteOrdonnance;
        this.fabricant = validerFabricant(fabricant);
        validerSeuils(stockMinimum, stockMaximum);
        this.stockMinimum = stockMinimum;
        this.stockMaximum = stockMaximum;
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
        Objects.requireNonNull(code, "Le code du médicament ne peut pas être null");
        String normalise = code.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(normalise).matches()) {
            throw new IllegalArgumentException(
                    "Le code du médicament doit être alphanumérique (2 à 30 caractères) : " + code);
        }
        return normalise;
    }

    private static String validerNomCommercial(String nomCommercial) {
        Objects.requireNonNull(nomCommercial, "Le nom commercial ne peut pas être null");
        String trimmed = nomCommercial.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom commercial ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le nom commercial ne peut pas dépasser " + NOM_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerDci(String dci) {
        Objects.requireNonNull(dci, "La DCI (dénomination commune internationale) ne peut pas être null");
        String trimmed = dci.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("La DCI ne peut pas être vide");
        }
        if (trimmed.length() > DCI_MAX_LENGTH) {
            throw new IllegalArgumentException("La DCI ne peut pas dépasser " + DCI_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerDosage(String dosage) {
        Objects.requireNonNull(dosage, "Le dosage ne peut pas être null");
        String trimmed = dosage.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le dosage ne peut pas être vide");
        }
        if (trimmed.length() > DOSAGE_MAX_LENGTH) {
            throw new IllegalArgumentException("Le dosage ne peut pas dépasser " + DOSAGE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerProgrammeSante(String programmeSante) {
        if (programmeSante == null) {
            return null;
        }
        String trimmed = programmeSante.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.length() > PROGRAMME_SANTE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le programme de santé ne peut pas dépasser " + PROGRAMME_SANTE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerFabricant(String fabricant) {
        if (fabricant == null) {
            return null;
        }
        String trimmed = fabricant.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.length() > FABRICANT_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le fabricant ne peut pas dépasser " + FABRICANT_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static Integer validerDelai(Integer delaiApprovisionnementJours) {
        if (delaiApprovisionnementJours == null) {
            return null;
        }
        if (delaiApprovisionnementJours < 0) {
            throw new IllegalArgumentException(
                    "Le délai d'approvisionnement ne peut pas être négatif");
        }
        return delaiApprovisionnementJours;
    }

    private static void validerSeuils(Integer stockMinimum, Integer stockMaximum) {
        if (stockMinimum != null && stockMinimum < 0) {
            throw new IllegalArgumentException("Le seuil minimum de stock ne peut pas être négatif");
        }
        if (stockMaximum != null && stockMaximum < 0) {
            throw new IllegalArgumentException("Le seuil maximum de stock ne peut pas être négatif");
        }
        if (stockMinimum != null && stockMaximum != null && stockMinimum > stockMaximum) {
            throw new IllegalArgumentException(
                    "Le seuil minimum de stock ne peut pas être supérieur au seuil maximum");
        }
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getCode() {
        return code;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public String getDci() {
        return dci;
    }

    public String getDosage() {
        return dosage;
    }

    public FormeId getFormeId() {
        return formeId;
    }

    public FamilleId getFamilleId() {
        return familleId;
    }

    public VoieAdministration getVoieAdministration() {
        return voieAdministration;
    }

    public TemperatureConservation getTemperatureConservation() {
        return temperatureConservation;
    }

    public String getProgrammeSante() {
        return programmeSante;
    }

    public Integer getDelaiApprovisionnementJours() {
        return delaiApprovisionnementJours;
    }

    public boolean isNecessiteOrdonnance() {
        return necessiteOrdonnance;
    }

    public String getFabricant() {
        return fabricant;
    }

    public Integer getStockMinimum() {
        return stockMinimum;
    }

    public Integer getStockMaximum() {
        return stockMaximum;
    }

    public boolean isActif() {
        return actif;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(code);
        result = prime * result + Objects.hashCode(nomCommercial);
        result = prime * result + Objects.hashCode(dci);
        result = prime * result + Objects.hashCode(dosage);
        result = prime * result + Objects.hashCode(formeId);
        result = prime * result + Objects.hashCode(familleId);
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
        Medicament other = (Medicament) obj;
        return actif == other.actif
                && necessiteOrdonnance == other.necessiteOrdonnance
                && Objects.equals(code, other.code)
                && Objects.equals(nomCommercial, other.nomCommercial)
                && Objects.equals(dci, other.dci)
                && Objects.equals(dosage, other.dosage)
                && Objects.equals(formeId, other.formeId)
                && Objects.equals(familleId, other.familleId)
                && voieAdministration == other.voieAdministration
                && temperatureConservation == other.temperatureConservation
                && Objects.equals(programmeSante, other.programmeSante)
                && Objects.equals(delaiApprovisionnementJours, other.delaiApprovisionnementJours)
                && Objects.equals(fabricant, other.fabricant)
                && Objects.equals(stockMinimum, other.stockMinimum)
                && Objects.equals(stockMaximum, other.stockMaximum);
    }
}
