package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.FamilleSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.FamilleMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FamilleJpaRepository;
import ministere.sante.senpna.medicament.infrastructure.persistence.specification.FamilleSpecifications;
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
public class FamilleRepositoryAdapter implements FamilleRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("code", "libelle", "actif", "createdAt",
            "updatedAt");

    private final FamilleJpaRepository familleJpaRepository;
    private final FamilleMapper familleMapper;

    public FamilleRepositoryAdapter(FamilleJpaRepository familleJpaRepository, FamilleMapper familleMapper) {
        this.familleJpaRepository = familleJpaRepository;
        this.familleMapper = familleMapper;
    }

    @Override
    public Optional<Famille> findById(FamilleId id) {
        return familleJpaRepository.findById(id.getValue()).map(familleMapper::toDomain);
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code) {
        return familleJpaRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public boolean existsByCodeIgnoreCaseAndIdNot(String code, FamilleId id) {
        return familleJpaRepository.existsByCodeIgnoreCaseAndIdNot(code, id.getValue());
    }

    @Override
    @Transactional
    public Famille save(Famille famille) {
        FamilleJpaEntity saved = familleJpaRepository.save(familleMapper.toEntity(famille));
        return familleMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Famille> search(FamilleSearchCriteria criteria, PageRequest pageRequest) {
        Specification<FamilleJpaEntity> specification = FamilleSpecifications.combiner(criteria.recherche(),
                criteria.actif());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<FamilleJpaEntity> page = familleJpaRepository.findAll(specification, pageable);
        List<Famille> content = page.getContent().stream().map(familleMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
