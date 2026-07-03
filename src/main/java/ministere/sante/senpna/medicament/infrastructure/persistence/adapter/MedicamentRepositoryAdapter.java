package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.MedicamentSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.MedicamentMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.MedicamentJpaRepository;
import ministere.sante.senpna.medicament.infrastructure.persistence.specification.MedicamentSpecifications;
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
public class MedicamentRepositoryAdapter implements MedicamentRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("code", "nomCommercial", "dci", "actif",
            "createdAt", "updatedAt");

    private final MedicamentJpaRepository medicamentJpaRepository;
    private final MedicamentMapper medicamentMapper;

    public MedicamentRepositoryAdapter(MedicamentJpaRepository medicamentJpaRepository,
            MedicamentMapper medicamentMapper) {
        this.medicamentJpaRepository = medicamentJpaRepository;
        this.medicamentMapper = medicamentMapper;
    }

    @Override
    public Optional<Medicament> findById(MedicamentId id) {
        return medicamentJpaRepository.findById(id.getValue()).map(medicamentMapper::toDomain);
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code) {
        return medicamentJpaRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public boolean existsByCodeIgnoreCaseAndIdNot(String code, MedicamentId id) {
        return medicamentJpaRepository.existsByCodeIgnoreCaseAndIdNot(code, id.getValue());
    }

    @Override
    @Transactional
    public Medicament save(Medicament medicament) {
        MedicamentJpaEntity saved = medicamentJpaRepository.save(medicamentMapper.toEntity(medicament));
        return medicamentMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Medicament> search(MedicamentSearchCriteria criteria, PageRequest pageRequest) {
        Specification<MedicamentJpaEntity> specification = MedicamentSpecifications.combiner(criteria.recherche(),
                criteria.familleId(), criteria.formeId(), criteria.actif());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<MedicamentJpaEntity> page = medicamentJpaRepository.findAll(specification, pageable);
        List<Medicament> content = page.getContent().stream().map(medicamentMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
