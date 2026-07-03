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
        return Lot.reconstruct(
                LotId.of(entity.getId()),
                entity.getNumeroLot(),
                MedicamentId.of(entity.getMedicamentId()),
                FournisseurId.of(entity.getFournisseurId()),
                entity.getDateFabrication(),
                entity.getDateExpiration(),
                entity.getPrixAchat(),
                entity.getPrixVente(),
                StatutLot.valueOf(entity.getStatut()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public LotJpaEntity toEntity(Lot lot) {
        LotJpaEntity entity = new LotJpaEntity(
                lot.getId().getValue(),
                lot.getNumeroLot(),
                lot.getMedicamentId().getValue(),
                lot.getFournisseurId().getValue(),
                lot.getDateFabrication(),
                lot.getDateExpiration(),
                lot.getPrixAchat(),
                lot.getPrixVente(),
                lot.getStatut().name());
        entity.setCreatedAt(lot.getCreatedAt());
        entity.setUpdatedAt(lot.getUpdatedAt());
        return entity;
    }
}
