package ministere.sante.senpna.medicament.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class MedicamentMapper {

    public Medicament toDomain(MedicamentJpaEntity entity) {
        return Medicament.builder()
            .id(MedicamentId.of(entity.getId()))
            .code(entity.getCode())
            .nomCommercial(entity.getNomCommercial())
            .dci(entity.getDci())
            .dosage(entity.getDosage())
            .formeId(FormeId.of(entity.getFormeId()))
            .familleId(FamilleId.of(entity.getFamilleId()))
            .voieAdministration(entity.getVoieAdministration())
            .temperatureConservation(entity.getTemperatureConservation())
            .programmeSante(entity.getProgrammeSante())
            .delaiApprovisionnementJours(entity.getDelaiApprovisionnementJours())
            .necessiteOrdonnance(entity.isNecessiteOrdonnance())
            .fabricant(entity.getFabricant())
            .stockMinimum(entity.getStockMinimum())
            .stockMaximum(entity.getStockMaximum())
            .actif(entity.isActif())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public MedicamentJpaEntity toEntity(Medicament medicament) {
        MedicamentJpaEntity entity = MedicamentJpaEntity.builder()
            .id(medicament.getId().getValue())
            .code(medicament.getCode())
            .nomCommercial(medicament.getNomCommercial())
            .dci(medicament.getDci())
            .dosage(medicament.getDosage())
            .formeId(medicament.getFormeId().getValue())
            .familleId(medicament.getFamilleId().getValue())
            .voieAdministration(medicament.getVoieAdministration())
            .temperatureConservation(medicament.getTemperatureConservation())
            .programmeSante(medicament.getProgrammeSante())
            .delaiApprovisionnementJours(medicament.getDelaiApprovisionnementJours())
            .necessiteOrdonnance(medicament.isNecessiteOrdonnance())
            .fabricant(medicament.getFabricant())
            .stockMinimum(medicament.getStockMinimum())
            .stockMaximum(medicament.getStockMaximum())
            .actif(medicament.isActif())
            .build();
        entity.setCreatedAt(medicament.getCreatedAt());
        entity.setUpdatedAt(medicament.getUpdatedAt());
        return entity;
    }
}
