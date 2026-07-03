package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.FormeSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FormeJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.FormeMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FormeJpaRepository;
import ministere.sante.senpna.medicament.infrastructure.persistence.specification.FormeSpecifications;
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
public class FormeRepositoryAdapter implements FormeRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("code", "libelle", "actif", "createdAt",
            "updatedAt");

    private final FormeJpaRepository formeJpaRepository;
    private final FormeMapper formeMapper;

    public FormeRepositoryAdapter(FormeJpaRepository formeJpaRepository, FormeMapper formeMapper) {
        this.formeJpaRepository = formeJpaRepository;
        this.formeMapper = formeMapper;
    }

    @Override
    public Optional<Forme> findById(FormeId id) {
        return formeJpaRepository.findById(id.getValue()).map(formeMapper::toDomain);
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code) {
        return formeJpaRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public boolean existsByCodeIgnoreCaseAndIdNot(String code, FormeId id) {
        return formeJpaRepository.existsByCodeIgnoreCaseAndIdNot(code, id.getValue());
    }

    @Override
    @Transactional
    public Forme save(Forme forme) {
        FormeJpaEntity saved = formeJpaRepository.save(formeMapper.toEntity(forme));
        return formeMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Forme> search(FormeSearchCriteria criteria, PageRequest pageRequest) {
        Specification<FormeJpaEntity> specification = FormeSpecifications.combiner(criteria.recherche(),
                criteria.actif());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<FormeJpaEntity> page = formeJpaRepository.findAll(specification, pageable);
        List<Forme> content = page.getContent().stream().map(formeMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
