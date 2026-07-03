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

    public MedicamentJpaEntity(UUID id, String code, String nomCommercial, String dci, String dosage, UUID formeId,
            UUID familleId, VoieAdministration voieAdministration, TemperatureConservation temperatureConservation,
            String programmeSante, Integer delaiApprovisionnementJours, boolean necessiteOrdonnance,
            String fabricant, Integer stockMinimum, Integer stockMaximum, boolean actif) {
        super(id);
        this.code = code;
        this.nomCommercial = nomCommercial;
        this.dci = dci;
        this.dosage = dosage;
        this.formeId = formeId;
        this.familleId = familleId;
        this.voieAdministration = voieAdministration;
        this.temperatureConservation = temperatureConservation;
        this.programmeSante = programmeSante;
        this.delaiApprovisionnementJours = delaiApprovisionnementJours;
        this.necessiteOrdonnance = necessiteOrdonnance;
        this.fabricant = fabricant;
        this.stockMinimum = stockMinimum;
        this.stockMaximum = stockMaximum;
        this.actif = actif;
    }
}
