package ministere.sante.senpna.stock.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public Stock toDomain(StockJpaEntity entity) {
        return Stock.builder()
            .id(StockId.of(entity.getId()))
            .entrepotId(EntrepotId.of(entity.getEntrepotId()))
            .lotId(LotId.of(entity.getLotId()))
            .medicamentId(MedicamentId.of(entity.getMedicamentId()))
            .quantiteDisponible(entity.getQuantiteDisponible())
            .quantiteReservee(entity.getQuantiteReservee())
            .quantiteEnCommande(entity.getQuantiteEnCommande())
            .seuilAlerte(entity.getSeuilAlerte())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public StockJpaEntity toEntity(Stock stock) {
        StockJpaEntity entity = StockJpaEntity.builder()
            .id(stock.getId().getValue())
            .entrepotId(stock.getEntrepotId().getValue())
            .lotId(stock.getLotId().getValue())
            .medicamentId(stock.getMedicamentId().getValue())
            .quantiteDisponible(stock.getQuantiteDisponible())
            .quantiteReservee(stock.getQuantiteReservee())
            .quantiteEnCommande(stock.getQuantiteEnCommande())
            .seuilAlerte(stock.getSeuilAlerte())
            .build();
        entity.setCreatedAt(stock.getCreatedAt());
        entity.setUpdatedAt(stock.getUpdatedAt());
        return entity;
    }
}
