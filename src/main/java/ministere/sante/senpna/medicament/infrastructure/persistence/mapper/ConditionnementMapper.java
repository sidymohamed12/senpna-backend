package ministere.sante.senpna.medicament.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class ConditionnementMapper {

    public Conditionnement toDomain(ConditionnementJpaEntity entity) {
        return Conditionnement.builder()
            .id(ConditionnementId.of(entity.getId()))
            .medicamentId(MedicamentId.of(entity.getMedicamentId()))
            .nom(entity.getNom())
            .niveau(entity.getNiveau())
            .quantiteUniteBase(entity.getQuantiteUniteBase())
            .estUniteBase(entity.isEstUniteBase())
            .prixAchat(entity.getPrixAchat())
            .prixVente(entity.getPrixVente())
            .actif(entity.isActif())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public ConditionnementJpaEntity toEntity(Conditionnement conditionnement) {
        ConditionnementJpaEntity entity = ConditionnementJpaEntity.builder()
            .id(conditionnement.getId().getValue())
            .medicamentId(conditionnement.getMedicamentId().getValue())
            .nom(conditionnement.getNom())
            .niveau(conditionnement.getNiveau())
            .quantiteUniteBase(conditionnement.getQuantiteUniteBase())
            .estUniteBase(conditionnement.isEstUniteBase())
            .prixAchat(conditionnement.getPrixAchat())
            .prixVente(conditionnement.getPrixVente())
            .actif(conditionnement.isActif())
            .build();
        entity.setCreatedAt(conditionnement.getCreatedAt());
        entity.setUpdatedAt(conditionnement.getUpdatedAt());
        return entity;
    }
}
