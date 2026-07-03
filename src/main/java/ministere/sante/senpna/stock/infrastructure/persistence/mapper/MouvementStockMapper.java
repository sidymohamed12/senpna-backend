package ministere.sante.senpna.stock.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class MouvementStockMapper {

    public MouvementStock toDomain(MouvementStockJpaEntity entity) {
        return MouvementStock.reconstruct(
                MouvementStockId.of(entity.getId()),
                TypeMouvement.valueOf(entity.getTypeMouvement()),
                SensMouvement.valueOf(entity.getSens()),
                entity.getEntrepotSourceId() != null ? EntrepotId.of(entity.getEntrepotSourceId()) : null,
                entity.getEntrepotDestinationId() != null ? EntrepotId.of(entity.getEntrepotDestinationId()) : null,
                entity.getCommandeId(),
                LotId.of(entity.getLotId()),
                MedicamentId.of(entity.getMedicamentId()),
                entity.getQuantite(),
                entity.getDateMouvement(),
                entity.getReferenceDocument(),
                entity.getMotif(),
                entity.getUtilisateurId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public MouvementStockJpaEntity toEntity(MouvementStock mouvement) {
        MouvementStockJpaEntity entity = new MouvementStockJpaEntity(
                mouvement.getId().getValue(),
                mouvement.getTypeMouvement().name(),
                mouvement.getSens().name(),
                mouvement.getEntrepotSourceId() != null ? mouvement.getEntrepotSourceId().getValue() : null,
                mouvement.getEntrepotDestinationId() != null ? mouvement.getEntrepotDestinationId().getValue()
                        : null,
                mouvement.getCommandeId(),
                mouvement.getLotId().getValue(),
                mouvement.getMedicamentId().getValue(),
                mouvement.getQuantite(),
                mouvement.getDateMouvement(),
                mouvement.getReferenceDocument(),
                mouvement.getMotif(),
                mouvement.getUtilisateurId());
        entity.setCreatedAt(mouvement.getCreatedAt());
        entity.setUpdatedAt(mouvement.getUpdatedAt());
        return entity;
    }
}
