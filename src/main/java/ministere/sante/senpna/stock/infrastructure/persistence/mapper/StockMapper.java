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
        return Stock.reconstruct(
                StockId.of(entity.getId()),
                EntrepotId.of(entity.getEntrepotId()),
                LotId.of(entity.getLotId()),
                MedicamentId.of(entity.getMedicamentId()),
                entity.getQuantiteDisponible(),
                entity.getQuantiteReservee(),
                entity.getQuantiteEnCommande(),
                entity.getSeuilAlerte(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public StockJpaEntity toEntity(Stock stock) {
        StockJpaEntity entity = new StockJpaEntity(
                stock.getId().getValue(),
                stock.getEntrepotId().getValue(),
                stock.getLotId().getValue(),
                stock.getMedicamentId().getValue(),
                stock.getQuantiteDisponible(),
                stock.getQuantiteReservee(),
                stock.getQuantiteEnCommande(),
                stock.getSeuilAlerte());
        entity.setCreatedAt(stock.getCreatedAt());
        entity.setUpdatedAt(stock.getUpdatedAt());
        return entity;
    }
}
