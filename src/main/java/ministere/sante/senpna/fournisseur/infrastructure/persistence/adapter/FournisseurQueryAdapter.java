package ministere.sante.senpna.fournisseur.infrastructure.persistence.adapter;

import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.repository.FournisseurJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.FournisseurQueryPort;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du port {@code shared} {@link FournisseurQueryPort} — le
 * module {@code fournisseur} reste le seul propriétaire de l'agrégat
 * {@code Fournisseur} et de sa persistance. Alimente
 * {@code FournisseurCache} au démarrage puis à chaque rechargement.
 * Symétrique à {@code RoleQueryAdapter} / {@code RegionQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class FournisseurQueryAdapter implements FournisseurQueryPort {

    private final FournisseurJpaRepository fournisseurJpaRepository;

    public FournisseurQueryAdapter(FournisseurJpaRepository fournisseurJpaRepository) {
        this.fournisseurJpaRepository = fournisseurJpaRepository;
    }

    @Override
    public List<FournisseurProjection> findAll() {
        return fournisseurJpaRepository.findAll().stream().map(this::toProjection).toList();
    }

    private FournisseurProjection toProjection(FournisseurJpaEntity entity) {
        return new FournisseurProjection(entity.getId(), entity.getNom(), entity.isActif());
    }
}
