package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.EntrepotJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du port {@code shared} {@link EntrepotQueryPort} — le
 * module {@code organisation} reste le seul propriétaire de l'agrégat
 * {@code Entrepot} et de sa persistance ; les autres features n'accèdent
 * qu'à cette projection en lecture. Symétrique à {@code RegionQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class EntrepotQueryAdapter implements EntrepotQueryPort {

    private final EntrepotJpaRepository entrepotJpaRepository;

    public EntrepotQueryAdapter(EntrepotJpaRepository entrepotJpaRepository) {
        this.entrepotJpaRepository = entrepotJpaRepository;
    }

    @Override
    public Optional<EntrepotProjection> findById(UUID id) {
        return entrepotJpaRepository.findById(id).map(this::toProjection);
    }

    @Override
    public boolean existsById(UUID id) {
        return entrepotJpaRepository.existsById(id);
    }

    @Override
    public Optional<EntrepotProjection> findPnaCentraleActive() {
        return entrepotJpaRepository.findFirstByTypeAndActifTrue(TypeEntrepot.PNA_CENTRAL).map(this::toProjection);
    }

    @Override
    public List<EntrepotProjection> findPrasActives() {
        return entrepotJpaRepository.findByTypeAndActifTrueOrderByCodeAsc(TypeEntrepot.PRA).stream()
                .map(this::toProjection)
                .toList();
    }

    @Override
    public List<EntrepotProjection> findPrasActivesParRegion(UUID regionId) {
        return entrepotJpaRepository.findByTypeAndRegionIdAndActifTrueOrderByCodeAsc(TypeEntrepot.PRA, regionId)
                .stream()
                .map(this::toProjection)
                .toList();
    }

    private EntrepotProjection toProjection(EntrepotJpaEntity entity) {
        return new EntrepotProjection(
                entity.getId(),
                entity.getCode(),
                entity.getNom(),
                entity.getType().name(),
                entity.getRegionId(),
                entity.isActif());
    }
}
