package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.MouvementSearchCriteria;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.MouvementStockMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.MouvementStockJpaRepository;
import ministere.sante.senpna.stock.infrastructure.persistence.specification.MouvementStockSpecifications;

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
public class MouvementStockRepositoryAdapter implements MouvementStockRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("dateMouvement", "createdAt");

    private final MouvementStockJpaRepository mouvementStockJpaRepository;
    private final MouvementStockMapper mouvementStockMapper;

    public MouvementStockRepositoryAdapter(MouvementStockJpaRepository mouvementStockJpaRepository,
            MouvementStockMapper mouvementStockMapper) {
        this.mouvementStockJpaRepository = mouvementStockJpaRepository;
        this.mouvementStockMapper = mouvementStockMapper;
    }

    @Override
    public Optional<MouvementStock> findById(MouvementStockId id) {
        return mouvementStockJpaRepository.findById(id.getValue()).map(mouvementStockMapper::toDomain);
    }

    @Override
    @Transactional
    public MouvementStock save(MouvementStock mouvementStock) {
        MouvementStockJpaEntity saved = mouvementStockJpaRepository.save(mouvementStockMapper.toEntity(mouvementStock));
        return mouvementStockMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MouvementStock> search(MouvementSearchCriteria criteria, PageRequest pageRequest) {
        Specification<MouvementStockJpaEntity> specification = MouvementStockSpecifications.combiner(
                criteria.lotId(), criteria.medicamentId(), criteria.entrepotId(), criteria.typeMouvement(),
                criteria.sens(), criteria.utilisateurId(), criteria.dateDebut(), criteria.dateFin());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy()
                : "dateMouvement";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<MouvementStockJpaEntity> page = mouvementStockJpaRepository.findAll(specification, pageable);
        List<MouvementStock> content = page.getContent().stream().map(mouvementStockMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
