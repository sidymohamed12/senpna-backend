package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.StockMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.StockJpaRepository;
import ministere.sante.senpna.stock.infrastructure.persistence.specification.StockSpecifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class StockRepositoryAdapter implements StockRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of(
            "quantiteDisponible", "quantiteReservee", "createdAt", "updatedAt");

    private final StockJpaRepository stockJpaRepository;
    private final StockMapper stockMapper;

    public StockRepositoryAdapter(StockJpaRepository stockJpaRepository, StockMapper stockMapper) {
        this.stockJpaRepository = stockJpaRepository;
        this.stockMapper = stockMapper;
    }

    @Override
    public Optional<Stock> findById(StockId id) {
        return stockJpaRepository.findById(id.getValue()).map(stockMapper::toDomain);
    }

    @Override
    public Optional<Stock> findByEntrepotIdAndLotId(EntrepotId entrepotId, LotId lotId) {
        return stockJpaRepository.findByEntrepotIdAndLotId(entrepotId.getValue(), lotId.getValue())
                .map(stockMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Stock> findByEntrepotIdAndLotIdForUpdate(EntrepotId entrepotId, LotId lotId) {
        return stockJpaRepository.findByEntrepotIdAndLotIdForUpdate(entrepotId.getValue(), lotId.getValue())
                .map(stockMapper::toDomain);
    }

    @Override
    @Transactional
    public Stock save(Stock stock) {
        StockJpaEntity saved = stockJpaRepository.save(stockMapper.toEntity(stock));
        return stockMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Stock> search(StockSearchCriteria criteria, PageRequest pageRequest) {
        Specification<StockJpaEntity> specification = StockSpecifications.combiner(
                criteria.entrepotId(), criteria.lotId(), criteria.medicamentId(), criteria.ruptureUniquement(),
                criteria.seuilAtteintUniquement());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<StockJpaEntity> page = stockJpaRepository.findAll(specification, pageable);
        List<Stock> content = page.getContent().stream().map(stockMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
