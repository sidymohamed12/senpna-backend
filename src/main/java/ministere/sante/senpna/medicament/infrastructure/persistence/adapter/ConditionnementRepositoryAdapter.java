package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.ConditionnementSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.ConditionnementMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.ConditionnementJpaRepository;
import ministere.sante.senpna.medicament.infrastructure.persistence.specification.ConditionnementSpecifications;
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
public class ConditionnementRepositoryAdapter implements ConditionnementRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("nom", "niveau", "actif", "createdAt",
            "updatedAt");

    private final ConditionnementJpaRepository conditionnementJpaRepository;
    private final ConditionnementMapper conditionnementMapper;

    public ConditionnementRepositoryAdapter(ConditionnementJpaRepository conditionnementJpaRepository,
            ConditionnementMapper conditionnementMapper) {
        this.conditionnementJpaRepository = conditionnementJpaRepository;
        this.conditionnementMapper = conditionnementMapper;
    }

    @Override
    public Optional<Conditionnement> findById(ConditionnementId id) {
        return conditionnementJpaRepository.findById(id.getValue()).map(conditionnementMapper::toDomain);
    }

    @Override
    public boolean existsByMedicamentIdAndNiveau(MedicamentId medicamentId, int niveau) {
        return conditionnementJpaRepository.existsByMedicamentIdAndNiveau(medicamentId.getValue(), niveau);
    }

    @Override
    public boolean existsByMedicamentIdAndNiveauAndIdNot(MedicamentId medicamentId, int niveau,
            ConditionnementId id) {
        return conditionnementJpaRepository.existsByMedicamentIdAndNiveauAndIdNot(medicamentId.getValue(), niveau,
                id.getValue());
    }

    @Override
    public boolean existsByMedicamentIdAndNomIgnoreCase(MedicamentId medicamentId, String nom) {
        return conditionnementJpaRepository.existsByMedicamentIdAndNomIgnoreCase(medicamentId.getValue(), nom);
    }

    @Override
    public boolean existsByMedicamentIdAndNomIgnoreCaseAndIdNot(MedicamentId medicamentId, String nom,
            ConditionnementId id) {
        return conditionnementJpaRepository.existsByMedicamentIdAndNomIgnoreCaseAndIdNot(medicamentId.getValue(),
                nom, id.getValue());
    }

    @Override
    public boolean existsUniteBaseByMedicamentId(MedicamentId medicamentId) {
        return conditionnementJpaRepository.existsByMedicamentIdAndEstUniteBaseTrue(medicamentId.getValue());
    }

    @Override
    public boolean existsUniteBaseByMedicamentIdAndIdNot(MedicamentId medicamentId, ConditionnementId id) {
        return conditionnementJpaRepository.existsByMedicamentIdAndEstUniteBaseTrueAndIdNot(medicamentId.getValue(),
                id.getValue());
    }

    @Override
    public boolean estUniqueUniteBaseActive(MedicamentId medicamentId, ConditionnementId id) {
        // Le conditionnement ciblé est encore actif au moment de la vérification
        // (l'archivage n'a pas encore eu lieu) : s'il est l'unique unité de base
        // active, ce compte vaut 1.
        return conditionnementJpaRepository
                .countByMedicamentIdAndEstUniteBaseTrueAndActifTrue(medicamentId.getValue()) <= 1;
    }

    @Override
    @Transactional
    public Conditionnement save(Conditionnement conditionnement) {
        ConditionnementJpaEntity saved = conditionnementJpaRepository
                .save(conditionnementMapper.toEntity(conditionnement));
        return conditionnementMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Conditionnement> search(ConditionnementSearchCriteria criteria, PageRequest pageRequest) {
        Specification<ConditionnementJpaEntity> specification = ConditionnementSpecifications.combiner(
                criteria.medicamentId(), criteria.actif());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "niveau";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<ConditionnementJpaEntity> page = conditionnementJpaRepository.findAll(specification, pageable);
        List<Conditionnement> content = page.getContent().stream().map(conditionnementMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
