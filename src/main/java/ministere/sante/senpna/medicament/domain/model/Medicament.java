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

    private Medicament(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.code = validerCode(builder.code);
        this.nomCommercial = validerNomCommercial(builder.nomCommercial);
        this.dci = validerDci(builder.dci);
        this.dosage = validerDosage(builder.dosage);
        this.formeId = Objects.requireNonNull(builder.formeId, "La forme pharmaceutique est obligatoire");
        this.familleId = Objects.requireNonNull(builder.familleId, "La famille thérapeutique est obligatoire");
        this.voieAdministration = builder.voieAdministration;
        this.temperatureConservation = builder.temperatureConservation != null ? builder.temperatureConservation
                : TemperatureConservation.AMBIANTE;
        this.programmeSante = validerProgrammeSante(builder.programmeSante);
        this.delaiApprovisionnementJours = validerDelai(builder.delaiApprovisionnementJours);
        this.necessiteOrdonnance = builder.necessiteOrdonnance;
        this.fabricant = validerFabricant(builder.fabricant);
        validerSeuils(builder.stockMinimum, builder.stockMaximum);
        this.stockMinimum = builder.stockMinimum;
        this.stockMaximum = builder.stockMaximum;
        this.actif = builder.actif;
    }

    /** Données nécessaires à la création d'un nouveau médicament. */
    public record CreationCommand(String code, String nomCommercial, String dci, String dosage, FormeId formeId,
            FamilleId familleId, VoieAdministration voieAdministration,
            TemperatureConservation temperatureConservation, String programmeSante,
            Integer delaiApprovisionnementJours, boolean necessiteOrdonnance, String fabricant,
            Integer stockMinimum, Integer stockMaximum) {
    }

    public static Medicament creer(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(MedicamentId.generate())
                .code(command.code())
                .nomCommercial(command.nomCommercial())
                .dci(command.dci())
                .dosage(command.dosage())
                .formeId(command.formeId())
                .familleId(command.familleId())
                .voieAdministration(command.voieAdministration())
                .temperatureConservation(command.temperatureConservation())
                .programmeSante(command.programmeSante())
                .delaiApprovisionnementJours(command.delaiApprovisionnementJours())
                .necessiteOrdonnance(command.necessiteOrdonnance())
                .fabricant(command.fabricant())
                .stockMinimum(command.stockMinimum())
                .stockMaximum(command.stockMaximum())
                .actif(true)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private MedicamentId id;
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
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(MedicamentId id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder nomCommercial(String nomCommercial) {
            this.nomCommercial = nomCommercial;
            return this;
        }

        public Builder dci(String dci) {
            this.dci = dci;
            return this;
        }

        public Builder dosage(String dosage) {
            this.dosage = dosage;
            return this;
        }

        public Builder formeId(FormeId formeId) {
            this.formeId = formeId;
            return this;
        }

        public Builder familleId(FamilleId familleId) {
            this.familleId = familleId;
            return this;
        }

        public Builder voieAdministration(VoieAdministration voieAdministration) {
            this.voieAdministration = voieAdministration;
            return this;
        }

        public Builder temperatureConservation(TemperatureConservation temperatureConservation) {
            this.temperatureConservation = temperatureConservation;
            return this;
        }

        public Builder programmeSante(String programmeSante) {
            this.programmeSante = programmeSante;
            return this;
        }

        public Builder delaiApprovisionnementJours(Integer delaiApprovisionnementJours) {
            this.delaiApprovisionnementJours = delaiApprovisionnementJours;
            return this;
        }

        public Builder necessiteOrdonnance(boolean necessiteOrdonnance) {
            this.necessiteOrdonnance = necessiteOrdonnance;
            return this;
        }

        public Builder fabricant(String fabricant) {
            this.fabricant = fabricant;
            return this;
        }

        public Builder stockMinimum(Integer stockMinimum) {
            this.stockMinimum = stockMinimum;
            return this;
        }

        public Builder stockMaximum(Integer stockMaximum) {
            this.stockMaximum = stockMaximum;
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

        public Medicament build() {
            return new Medicament(this);
        }
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
