package ministere.sante.senpna.carriere.infrastructure.persistence.adapter;

import ministere.sante.senpna.carriere.domain.criteria.CandidatureSearchCriteria;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.CandidatureId;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.CandidatureJpaEntity;
import ministere.sante.senpna.carriere.infrastructure.persistence.mapper.CandidatureMapper;
import ministere.sante.senpna.carriere.infrastructure.persistence.repository.CandidatureJpaRepository;
import ministere.sante.senpna.carriere.infrastructure.persistence.specification.CandidatureSpecifications;
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
public class CandidatureRepositoryAdapter implements CandidatureRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("nomComplet", "email", "createdAt");

    private final CandidatureJpaRepository candidatureJpaRepository;
    private final CandidatureMapper candidatureMapper;

    public CandidatureRepositoryAdapter(CandidatureJpaRepository candidatureJpaRepository,
            CandidatureMapper candidatureMapper) {
        this.candidatureJpaRepository = candidatureJpaRepository;
        this.candidatureMapper = candidatureMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Candidature> findById(CandidatureId id) {
        return candidatureJpaRepository.findById(id.getValue()).map(candidatureMapper::toDomain);
    }

    @Override
    @Transactional
    public Candidature save(Candidature candidature) {
        // Une candidature n'est jamais modifiée après soumission (cf.
        // CandidatureMapper) : toujours un INSERT.
        CandidatureJpaEntity entity = candidatureMapper.toNewEntity(candidature);
        CandidatureJpaEntity saved = candidatureJpaRepository.save(entity);
        return candidatureMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Candidature> search(CandidatureSearchCriteria criteria, PageRequest pageRequest) {
        Specification<CandidatureJpaEntity> specification = CandidatureSpecifications.combiner(
                criteria.opportuniteId(), criteria.recherche());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<CandidatureJpaEntity> page = candidatureJpaRepository.findAll(specification, pageable);
        List<Candidature> content = page.getContent().stream().map(candidatureMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
