package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.infrastructure.persistence.entity.RegionJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.RegionJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.RegionQueryPort;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du port {@code shared} {@link RegionQueryPort} — le
 * module {@code organisation} reste le seul propriétaire de l'agrégat
 * {@code Region} et de sa persistance ; les autres features n'accèdent
 * qu'à cette projection en lecture, jamais à {@code RegionJpaEntity}
 * directement. Symétrique à {@code RoleQueryAdapter} (module {@code auth}).
 */
@Component
@Transactional(readOnly = true)
public class RegionQueryAdapter implements RegionQueryPort {

    private final RegionJpaRepository regionJpaRepository;

    public RegionQueryAdapter(RegionJpaRepository regionJpaRepository) {
        this.regionJpaRepository = regionJpaRepository;
    }

    @Override
    public List<RegionProjection> findAll() {
        return regionJpaRepository.findAll()
                .stream()
                .map(this::toProjection)
                .toList();
    }

    @Override
    public Optional<RegionProjection> findByCode(String code) {
        return regionJpaRepository.findByCode(code).map(this::toProjection);
    }

    @Override
    public Optional<RegionProjection> findById(UUID id) {
        return regionJpaRepository.findById(id).map(this::toProjection);
    }

    @Override
    public boolean existsById(UUID regionId) {
        return regionJpaRepository.existsById(regionId);
    }

    private RegionProjection toProjection(RegionJpaEntity entity) {
        return new RegionProjection(entity.getId(), entity.getCode(), entity.getNom(), entity.isActif());
    }
}
