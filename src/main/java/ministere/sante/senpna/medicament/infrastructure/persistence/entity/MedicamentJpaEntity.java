package ministere.sante.senpna.medicament.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "medicaments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class MedicamentJpaEntity extends BaseJpaEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "nom_commercial", nullable = false, length = 200)
    private String nomCommercial;

    @Column(name = "dci", nullable = false, length = 200)
    private String dci;

    @Column(name = "dosage", nullable = false, length = 50)
    private String dosage;

    @Column(name = "forme_id", nullable = false)
    private UUID formeId;

    @Column(name = "famille_id", nullable = false)
    private UUID familleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "voie_administration", length = 20)
    private VoieAdministration voieAdministration;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_conservation", nullable = false, length = 30)
    private TemperatureConservation temperatureConservation;

    @Column(name = "programme_sante", length = 150)
    private String programmeSante;

    @Column(name = "delai_approvisionnement_jours")
    private Integer delaiApprovisionnementJours;

    @Column(name = "necessite_ordonnance", nullable = false)
    private boolean necessiteOrdonnance;

    @Column(name = "fabricant", length = 150)
    private String fabricant;

    @Column(name = "stock_minimum")
    private Integer stockMinimum;

    @Column(name = "stock_maximum")
    private Integer stockMaximum;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    private MedicamentJpaEntity(Builder builder) {
        super(builder.id);
        this.code = builder.code;
        this.nomCommercial = builder.nomCommercial;
        this.dci = builder.dci;
        this.dosage = builder.dosage;
        this.formeId = builder.formeId;
        this.familleId = builder.familleId;
        this.voieAdministration = builder.voieAdministration;
        this.temperatureConservation = builder.temperatureConservation;
        this.programmeSante = builder.programmeSante;
        this.delaiApprovisionnementJours = builder.delaiApprovisionnementJours;
        this.necessiteOrdonnance = builder.necessiteOrdonnance;
        this.fabricant = builder.fabricant;
        this.stockMinimum = builder.stockMinimum;
        this.stockMaximum = builder.stockMaximum;
        this.actif = builder.actif;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String code;
        private String nomCommercial;
        private String dci;
        private String dosage;
        private UUID formeId;
        private UUID familleId;
        private VoieAdministration voieAdministration;
        private TemperatureConservation temperatureConservation;
        private String programmeSante;
        private Integer delaiApprovisionnementJours;
        private boolean necessiteOrdonnance;
        private String fabricant;
        private Integer stockMinimum;
        private Integer stockMaximum;
        private boolean actif;

        private Builder() {
        }

        public Builder id(UUID id) {
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

        public Builder formeId(UUID formeId) {
            this.formeId = formeId;
            return this;
        }

        public Builder familleId(UUID familleId) {
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

        public MedicamentJpaEntity build() {
            return new MedicamentJpaEntity(this);
        }
    }

}
