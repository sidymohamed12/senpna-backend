package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.criteria.StructureSanitaireSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.StructureSanitaireMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.StructureSanitaireJpaRepository;
import ministere.sante.senpna.organisation.infrastructure.persistence.specification.StructureSanitaireSpecifications;
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
public class StructureSanitaireRepositoryAdapter implements StructureSanitaireRepositoryPort {

        private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of(
                        "code", "nom", "type", "statutAdhesion", "actif", "createdAt", "updatedAt");

        private final StructureSanitaireJpaRepository structureSanitaireJpaRepository;
        private final StructureSanitaireMapper structureSanitaireMapper;

        public StructureSanitaireRepositoryAdapter(StructureSanitaireJpaRepository structureSanitaireJpaRepository,
                        StructureSanitaireMapper structureSanitaireMapper) {
                this.structureSanitaireJpaRepository = structureSanitaireJpaRepository;
                this.structureSanitaireMapper = structureSanitaireMapper;
        }

        @Override
        public Optional<StructureSanitaire> findById(StructureSanitaireId id) {
                return structureSanitaireJpaRepository.findById(id.getValue()).map(structureSanitaireMapper::toDomain);
        }

        @Override
        public boolean existsByCode(String code) {
                return structureSanitaireJpaRepository.existsByCode(code);
        }

        @Override
        @Transactional
        public StructureSanitaire save(StructureSanitaire structureSanitaire) {
                StructureSanitaireJpaEntity saved = structureSanitaireJpaRepository
                                .save(structureSanitaireMapper.toEntity(structureSanitaire));
                return structureSanitaireMapper.toDomain(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResult<StructureSanitaire> search(StructureSanitaireSearchCriteria criteria,
                        PageRequest pageRequest) {
                Specification<StructureSanitaireJpaEntity> specification = StructureSanitaireSpecifications.combiner(
                                criteria.recherche(), criteria.type(), criteria.regionId(), criteria.praId(),
                                criteria.statutAdhesion(), criteria.actif());

                Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy()
                                : "createdAt";

                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

                Page<StructureSanitaireJpaEntity> page = structureSanitaireJpaRepository.findAll(specification,
                                pageable);
                List<StructureSanitaire> content = page.getContent().stream().map(structureSanitaireMapper::toDomain)
                                .toList();

                return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
        }
}
