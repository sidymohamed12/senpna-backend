package ministere.sante.senpna.carriere.infrastructure.persistence.adapter;

import ministere.sante.senpna.carriere.domain.criteria.OpportuniteCarriereSearchCriteria;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.OpportuniteCarriereJpaEntity;
import ministere.sante.senpna.carriere.infrastructure.persistence.mapper.OpportuniteCarriereMapper;
import ministere.sante.senpna.carriere.infrastructure.persistence.repository.OpportuniteCarriereJpaRepository;
import ministere.sante.senpna.carriere.infrastructure.persistence.specification.OpportuniteCarriereSpecifications;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class OpportuniteCarriereRepositoryAdapter implements OpportuniteCarriereRepositoryPort {

        private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("titre", "nomEntreprise", "typeContrat",
                        "dateLimiteCandidature", "statut", "createdAt", "updatedAt");

        private final OpportuniteCarriereJpaRepository opportuniteCarriereJpaRepository;
        private final OpportuniteCarriereMapper opportuniteCarriereMapper;

        public OpportuniteCarriereRepositoryAdapter(OpportuniteCarriereJpaRepository opportuniteCarriereJpaRepository,
                        OpportuniteCarriereMapper opportuniteCarriereMapper) {
                this.opportuniteCarriereJpaRepository = opportuniteCarriereJpaRepository;
                this.opportuniteCarriereMapper = opportuniteCarriereMapper;
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<OpportuniteCarriere> findById(OpportuniteCarriereId id) {
                return opportuniteCarriereJpaRepository.findById(id.getValue())
                                .map(opportuniteCarriereMapper::toDomain);
        }

        @Override
        @Transactional
        public OpportuniteCarriere save(OpportuniteCarriere opportunite) {
                OpportuniteCarriereJpaEntity entity = opportuniteCarriereJpaRepository
                                .findById(opportunite.getId().getValue())
                                .map(existing -> opportuniteCarriereMapper.updateEntity(existing, opportunite))
                                .orElseGet(() -> opportuniteCarriereMapper.toNewEntity(opportunite));

                OpportuniteCarriereJpaEntity saved = opportuniteCarriereJpaRepository.save(entity);
                return opportuniteCarriereMapper.toDomain(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResult<OpportuniteCarriere> search(OpportuniteCarriereSearchCriteria criteria,
                        PageRequest pageRequest) {
                Specification<OpportuniteCarriereJpaEntity> specification = OpportuniteCarriereSpecifications.combiner(
                                criteria.recherche(), criteria.typeContrat(), criteria.statut(), criteria.publicOnly());

                Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy()
                                : "createdAt";

                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

                Page<OpportuniteCarriereJpaEntity> page = opportuniteCarriereJpaRepository.findAll(specification,
                                pageable);
                List<OpportuniteCarriere> content = page.getContent().stream().map(opportuniteCarriereMapper::toDomain)
                                .toList();

                return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
        }

        @Override
        @Transactional(readOnly = true)
        public List<OpportuniteCarriere> findOuvertesExpirees(LocalDate reference) {
                return opportuniteCarriereJpaRepository
                                .findByStatutInAndDateLimiteCandidatureBefore(
                                                List.of(StatutOpportunite.OUVERT, StatutOpportunite.EN_COURS),
                                                reference)
                                .stream()
                                .map(opportuniteCarriereMapper::toDomain)
                                .toList();
        }
}
