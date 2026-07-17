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
        return MouvementStock.builder()
            .id(MouvementStockId.of(entity.getId()))
            .typeMouvement(TypeMouvement.valueOf(entity.getTypeMouvement()))
            .sens(SensMouvement.valueOf(entity.getSens()))
            .entrepotSourceId(entity.getEntrepotSourceId() != null ? EntrepotId.of(entity.getEntrepotSourceId()) : null)
            .entrepotDestinationId(entity.getEntrepotDestinationId() != null ? EntrepotId.of(entity.getEntrepotDestinationId()) : null)
            .commandeId(entity.getCommandeId())
            .lotId(LotId.of(entity.getLotId()))
            .medicamentId(MedicamentId.of(entity.getMedicamentId()))
            .quantite(entity.getQuantite())
            .dateMouvement(entity.getDateMouvement())
            .referenceDocument(entity.getReferenceDocument())
            .motif(entity.getMotif())
            .utilisateurId(entity.getUtilisateurId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public MouvementStockJpaEntity toEntity(MouvementStock mouvement) {
        MouvementStockJpaEntity entity = MouvementStockJpaEntity.builder()
            .id(mouvement.getId().getValue())
            .typeMouvement(mouvement.getTypeMouvement().name())
            .sens(mouvement.getSens().name())
            .entrepotSourceId(mouvement.getEntrepotSourceId() != null ? mouvement.getEntrepotSourceId().getValue() : null)
            .entrepotDestinationId(mouvement.getEntrepotDestinationId() != null ? mouvement.getEntrepotDestinationId().getValue()
                        : null)
            .commandeId(mouvement.getCommandeId())
            .lotId(mouvement.getLotId().getValue())
            .medicamentId(mouvement.getMedicamentId().getValue())
            .quantite(mouvement.getQuantite())
            .dateMouvement(mouvement.getDateMouvement())
            .referenceDocument(mouvement.getReferenceDocument())
            .motif(mouvement.getMotif())
            .utilisateurId(mouvement.getUtilisateurId())
            .build();
        entity.setCreatedAt(mouvement.getCreatedAt());
        entity.setUpdatedAt(mouvement.getUpdatedAt());
        return entity;
    }
}
