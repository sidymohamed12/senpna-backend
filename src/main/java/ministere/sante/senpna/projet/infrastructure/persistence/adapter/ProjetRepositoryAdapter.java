package ministere.sante.senpna.projet.infrastructure.persistence.adapter;

import ministere.sante.senpna.projet.domain.criteria.ProjetSearchCriteria;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.infrastructure.persistence.entity.ProjetJpaEntity;
import ministere.sante.senpna.projet.infrastructure.persistence.mapper.ProjetMapper;
import ministere.sante.senpna.projet.infrastructure.persistence.repository.ProjetJpaRepository;
import ministere.sante.senpna.projet.infrastructure.persistence.specification.ProjetSpecifications;
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
public class ProjetRepositoryAdapter implements ProjetRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of(
            "nom", "categorie", "statut", "createdAt", "updatedAt");

    private final ProjetJpaRepository projetJpaRepository;
    private final ProjetMapper projetMapper;

    public ProjetRepositoryAdapter(ProjetJpaRepository projetJpaRepository, ProjetMapper projetMapper) {
        this.projetJpaRepository = projetJpaRepository;
        this.projetMapper = projetMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Projet> findById(ProjetId id) {
        return projetJpaRepository.findById(id.getValue()).map(projetMapper::toDomain);
    }

    @Override
    @Transactional
    public Projet save(Projet projet) {
        ProjetJpaEntity saved = projetJpaRepository.save(projetMapper.toEntity(projet));
        return projetMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Projet> search(ProjetSearchCriteria criteria, PageRequest pageRequest) {
        Specification<ProjetJpaEntity> specification = ProjetSpecifications.combiner(
                criteria.recherche(), criteria.categorie(), criteria.statut());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<ProjetJpaEntity> page = projetJpaRepository.findAll(specification, pageable);
        List<Projet> content = page.getContent().stream().map(projetMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
