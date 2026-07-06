package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FormeJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FormeJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.FormeQueryPort;
import ministere.sante.senpna.shared.domain.projection.FormeProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du port {@code shared} {@link FormeQueryPort} — le
 * module {@code medicament} reste le seul propriétaire de l'agrégat
 * {@code Forme} et de sa persistance ; les autres features n'accèdent
 * qu'à cette projection en lecture, jamais à {@code FormeJpaEntity}
 * directement. Alimente {@code FormeCache} au démarrage puis à chaque
 * rechargement. Symétrique à {@code RegionQueryAdapter} /
 * {@code FournisseurQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class FormeQueryAdapter implements FormeQueryPort {

    private final FormeJpaRepository formeJpaRepository;

    public FormeQueryAdapter(FormeJpaRepository formeJpaRepository) {
        this.formeJpaRepository = formeJpaRepository;
    }

    @Override
    public List<FormeProjection> findAll() {
        return formeJpaRepository.findAll().stream().map(this::toProjection).toList();
    }

    private FormeProjection toProjection(FormeJpaEntity entity) {
        return new FormeProjection(entity.getId(), entity.getCode(), entity.getLibelle(), entity.isActif());
    }
}
