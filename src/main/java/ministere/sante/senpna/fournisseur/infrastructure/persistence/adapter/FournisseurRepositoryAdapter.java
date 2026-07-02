package ministere.sante.senpna.fournisseur.infrastructure.persistence.adapter;

import ministere.sante.senpna.fournisseur.domain.criteria.FournisseurSearchCriteria;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.mapper.FournisseurMapper;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.repository.FournisseurJpaRepository;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.specification.FournisseurSpecifications;
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
public class FournisseurRepositoryAdapter implements FournisseurRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("nom", "actif", "createdAt", "updatedAt");

    private final FournisseurJpaRepository fournisseurJpaRepository;
    private final FournisseurMapper fournisseurMapper;

    public FournisseurRepositoryAdapter(FournisseurJpaRepository fournisseurJpaRepository,
            FournisseurMapper fournisseurMapper) {
        this.fournisseurJpaRepository = fournisseurJpaRepository;
        this.fournisseurMapper = fournisseurMapper;
    }

    @Override
    public Optional<Fournisseur> findById(FournisseurId id) {
        return fournisseurJpaRepository.findById(id.getValue()).map(fournisseurMapper::toDomain);
    }

    @Override
    public boolean existsByNomIgnoreCase(String nom) {
        return fournisseurJpaRepository.existsByNomIgnoreCase(nom);
    }

    @Override
    public boolean existsByNomIgnoreCaseAndIdNot(String nom, FournisseurId id) {
        return fournisseurJpaRepository.existsByNomIgnoreCaseAndIdNot(nom, id.getValue());
    }

    @Override
    @Transactional
    public Fournisseur save(Fournisseur fournisseur) {
        FournisseurJpaEntity saved = fournisseurJpaRepository.save(fournisseurMapper.toEntity(fournisseur));
        return fournisseurMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Fournisseur> search(FournisseurSearchCriteria criteria, PageRequest pageRequest) {
        Specification<FournisseurJpaEntity> specification = FournisseurSpecifications.combiner(
                criteria.recherche(), criteria.actif());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<FournisseurJpaEntity> page = fournisseurJpaRepository.findAll(specification, pageable);
        List<Fournisseur> content = page.getContent().stream().map(fournisseurMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
