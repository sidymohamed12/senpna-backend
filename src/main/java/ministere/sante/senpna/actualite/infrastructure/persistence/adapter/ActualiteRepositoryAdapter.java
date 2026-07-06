package ministere.sante.senpna.actualite.infrastructure.persistence.adapter;

import ministere.sante.senpna.actualite.domain.criteria.ActualiteSearchCriteria;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteJpaEntity;
import ministere.sante.senpna.actualite.infrastructure.persistence.mapper.ActualiteMapper;
import ministere.sante.senpna.actualite.infrastructure.persistence.repository.ActualiteJpaRepository;
import ministere.sante.senpna.actualite.infrastructure.persistence.specification.ActualiteSpecifications;
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
public class ActualiteRepositoryAdapter implements ActualiteRepositoryPort {

        private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("titre", "categorie", "statut", "createdAt",
                        "updatedAt");

        private final ActualiteJpaRepository actualiteJpaRepository;
        private final ActualiteMapper actualiteMapper;

        public ActualiteRepositoryAdapter(ActualiteJpaRepository actualiteJpaRepository,
                        ActualiteMapper actualiteMapper) {
                this.actualiteJpaRepository = actualiteJpaRepository;
                this.actualiteMapper = actualiteMapper;
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<Actualite> findById(ActualiteId id) {
                return actualiteJpaRepository.findById(id.getValue()).map(actualiteMapper::toDomain);
        }

        @Override
        @Transactional
        public Actualite save(Actualite actualite) {
                // Réutilise l'entité managée existante (si présente) pour que la
                // collection Hibernate des médias reste cohérente et que
                // `orphanRemoval` supprime effectivement les médias retirés.
                ActualiteJpaEntity entity = actualiteJpaRepository.findById(actualite.getId().getValue())
                                .map(existing -> actualiteMapper.updateEntity(existing, actualite))
                                .orElseGet(() -> actualiteMapper.toNewEntity(actualite));

                ActualiteJpaEntity saved = actualiteJpaRepository.save(entity);
                return actualiteMapper.toDomain(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResult<Actualite> search(ActualiteSearchCriteria criteria, PageRequest pageRequest) {
                Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.combiner(
                                criteria.recherche(), criteria.categorie(), criteria.statut());

                Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy()
                                : "createdAt";

                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

                Page<ActualiteJpaEntity> page = actualiteJpaRepository.findAll(specification, pageable);
                List<Actualite> content = page.getContent().stream().map(actualiteMapper::toDomain).toList();

                return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
        }
}
