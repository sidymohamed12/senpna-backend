package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.criteria.EntrepotSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.EntrepotMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.EntrepotJpaRepository;
import ministere.sante.senpna.organisation.infrastructure.persistence.specification.EntrepotSpecifications;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

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
public class EntrepotRepositoryAdapter implements EntrepotRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of(
            "code", "nom", "type", "actif", "createdAt", "updatedAt");

    private final EntrepotJpaRepository entrepotJpaRepository;
    private final EntrepotMapper entrepotMapper;

    public EntrepotRepositoryAdapter(EntrepotJpaRepository entrepotJpaRepository, EntrepotMapper entrepotMapper) {
        this.entrepotJpaRepository = entrepotJpaRepository;
        this.entrepotMapper = entrepotMapper;
    }

    @Override
    public Optional<Entrepot> findById(EntrepotId id) {
        return entrepotJpaRepository.findById(id.getValue()).map(entrepotMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return entrepotJpaRepository.existsByCode(code);
    }

    @Override
    @Transactional
    public Entrepot save(Entrepot entrepot) {
        EntrepotJpaEntity saved = entrepotJpaRepository.save(entrepotMapper.toEntity(entrepot));
        return entrepotMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Entrepot> search(EntrepotSearchCriteria criteria, PageRequest pageRequest) {
        Specification<EntrepotJpaEntity> specification = EntrepotSpecifications.combiner(
                criteria.recherche(), criteria.type(), criteria.regionId(), criteria.actif());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<EntrepotJpaEntity> page = entrepotJpaRepository.findAll(specification, pageable);
        List<Entrepot> content = page.getContent().stream().map(entrepotMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
