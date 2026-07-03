package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.MedicamentJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation du port {@code shared} {@link MedicamentQueryPort} — le
 * module {@code medicament} reste le seul propriétaire de l'agrégat
 * {@code Medicament} et de sa persistance ; les autres features
 * (notamment {@code catalogue}) n'accèdent qu'à cette projection en
 * lecture. Symétrique à {@code EntrepotQueryAdapter} / {@code RegionQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class MedicamentQueryAdapter implements MedicamentQueryPort {

    private final MedicamentJpaRepository medicamentJpaRepository;

    public MedicamentQueryAdapter(MedicamentJpaRepository medicamentJpaRepository) {
        this.medicamentJpaRepository = medicamentJpaRepository;
    }

    @Override
    public List<MedicamentProjection> findAllById(Collection<UUID> ids) {
        return medicamentJpaRepository.findAllById(ids).stream().map(this::toProjection).toList();
    }

    private MedicamentProjection toProjection(MedicamentJpaEntity entity) {
        return new MedicamentProjection(
                entity.getId(),
                entity.getCode(),
                entity.getNomCommercial(),
                entity.getDci(),
                entity.getDosage(),
                entity.isNecessiteOrdonnance(),
                entity.isActif());
    }
}
