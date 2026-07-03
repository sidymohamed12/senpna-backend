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
        return Medicament.reconstruct(
                MedicamentId.of(entity.getId()),
                entity.getCode(),
                entity.getNomCommercial(),
                entity.getDci(),
                entity.getDosage(),
                FormeId.of(entity.getFormeId()),
                FamilleId.of(entity.getFamilleId()),
                entity.getVoieAdministration(),
                entity.getTemperatureConservation(),
                entity.getProgrammeSante(),
                entity.getDelaiApprovisionnementJours(),
                entity.isNecessiteOrdonnance(),
                entity.getFabricant(),
                entity.getStockMinimum(),
                entity.getStockMaximum(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public MedicamentJpaEntity toEntity(Medicament medicament) {
        MedicamentJpaEntity entity = new MedicamentJpaEntity(
                medicament.getId().getValue(),
                medicament.getCode(),
                medicament.getNomCommercial(),
                medicament.getDci(),
                medicament.getDosage(),
                medicament.getFormeId().getValue(),
                medicament.getFamilleId().getValue(),
                medicament.getVoieAdministration(),
                medicament.getTemperatureConservation(),
                medicament.getProgrammeSante(),
                medicament.getDelaiApprovisionnementJours(),
                medicament.isNecessiteOrdonnance(),
                medicament.getFabricant(),
                medicament.getStockMinimum(),
                medicament.getStockMaximum(),
                medicament.isActif());
        entity.setCreatedAt(medicament.getCreatedAt());
        entity.setUpdatedAt(medicament.getUpdatedAt());
        return entity;
    }
}
