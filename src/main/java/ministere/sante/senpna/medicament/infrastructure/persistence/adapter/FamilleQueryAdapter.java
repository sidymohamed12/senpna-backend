package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FamilleJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.FamilleQueryPort;
import ministere.sante.senpna.shared.domain.projection.FamilleProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du port {@code shared} {@link FamilleQueryPort} — le
 * module {@code medicament} reste le seul propriétaire de l'agrégat
 * {@code Famille} et de sa persistance. Alimente {@code FamilleCache} au
 * démarrage puis à chaque rechargement. Symétrique à
 * {@code FormeQueryAdapter} / {@code RegionQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class FamilleQueryAdapter implements FamilleQueryPort {

    private final FamilleJpaRepository familleJpaRepository;

    public FamilleQueryAdapter(FamilleJpaRepository familleJpaRepository) {
        this.familleJpaRepository = familleJpaRepository;
    }

    @Override
    public List<FamilleProjection> findAll() {
        return familleJpaRepository.findAll().stream().map(this::toProjection).toList();
    }

    private FamilleProjection toProjection(FamilleJpaEntity entity) {
        return new FamilleProjection(entity.getId(), entity.getCode(), entity.getLibelle(), entity.isActif());
    }
}
