package ministere.sante.senpna.stock.infrastructure.persistence.mapper;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class LotMapper {

    public Lot toDomain(LotJpaEntity entity) {
        return Lot.builder()
            .id(LotId.of(entity.getId()))
            .numeroLot(entity.getNumeroLot())
            .medicamentId(MedicamentId.of(entity.getMedicamentId()))
            .fournisseurId(FournisseurId.of(entity.getFournisseurId()))
            .dateFabrication(entity.getDateFabrication())
            .dateExpiration(entity.getDateExpiration())
            .prixAchat(entity.getPrixAchat())
            .prixVente(entity.getPrixVente())
            .statut(StatutLot.valueOf(entity.getStatut()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public LotJpaEntity toEntity(Lot lot) {
        LotJpaEntity entity = LotJpaEntity.builder()
            .id(lot.getId().getValue())
            .numeroLot(lot.getNumeroLot())
            .medicamentId(lot.getMedicamentId().getValue())
            .fournisseurId(lot.getFournisseurId().getValue())
            .dateFabrication(lot.getDateFabrication())
            .dateExpiration(lot.getDateExpiration())
            .prixAchat(lot.getPrixAchat())
            .prixVente(lot.getPrixVente())
            .statut(lot.getStatut().name())
            .build();
        entity.setCreatedAt(lot.getCreatedAt());
        entity.setUpdatedAt(lot.getUpdatedAt());
        return entity;
    }
}
