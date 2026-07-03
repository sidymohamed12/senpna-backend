package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.StructureSanitaireJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.StructureSanitaireQueryPort;
import ministere.sante.senpna.shared.domain.projection.StructureSanitaireProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du port {@code shared} {@link StructureSanitaireQueryPort}
 * — le module {@code organisation} reste le seul propriétaire de l'agrégat
 * {@code StructureSanitaire} et de sa persistance ; les autres features
 * n'accèdent qu'à cette projection en lecture. Symétrique à
 * {@code EntrepotQueryAdapter} / {@code RegionQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class StructureSanitaireQueryAdapter implements StructureSanitaireQueryPort {

    private final StructureSanitaireJpaRepository structureSanitaireJpaRepository;

    public StructureSanitaireQueryAdapter(StructureSanitaireJpaRepository structureSanitaireJpaRepository) {
        this.structureSanitaireJpaRepository = structureSanitaireJpaRepository;
    }

    @Override
    public Optional<StructureSanitaireProjection> findById(UUID id) {
        return structureSanitaireJpaRepository.findById(id).map(this::toProjection);
    }

    private StructureSanitaireProjection toProjection(StructureSanitaireJpaEntity entity) {
        return new StructureSanitaireProjection(
                entity.getId(),
                entity.getCode(),
                entity.getNom(),
                entity.getRegionId(),
                entity.getPraId(),
                entity.isActif());
    }
}
