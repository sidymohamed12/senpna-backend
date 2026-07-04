package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.ConditionnementJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.ConditionnementQueryPort;
import ministere.sante.senpna.shared.domain.projection.ConditionnementProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation du port {@code shared} {@link ConditionnementQueryPort}
 * — le module {@code medicament} reste le seul propriétaire de l'agrégat
 * {@code Conditionnement} et de sa persistance ; les autres features
 * (notamment {@code catalogue}) n'accèdent qu'à cette projection en
 * lecture, filtrée aux conditionnements réellement achetables. Symétrique
 * à {@code MedicamentQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class ConditionnementQueryAdapter implements ConditionnementQueryPort {

    private final ConditionnementJpaRepository conditionnementJpaRepository;

    public ConditionnementQueryAdapter(ConditionnementJpaRepository conditionnementJpaRepository) {
        this.conditionnementJpaRepository = conditionnementJpaRepository;
    }

    @Override
    public List<ConditionnementProjection> findAllVendablesByMedicamentIdIn(Collection<UUID> medicamentIds) {
        if (medicamentIds == null || medicamentIds.isEmpty()) {
            return List.of();
        }
        return conditionnementJpaRepository
                .findByMedicamentIdInAndActifTrueAndPrixVenteIsNotNullOrderByMedicamentIdAscNiveauAsc(medicamentIds)
                .stream()
                .map(this::toProjection)
                .toList();
    }

    private ConditionnementProjection toProjection(ConditionnementJpaEntity entity) {
        return new ConditionnementProjection(
                entity.getId(),
                entity.getMedicamentId(),
                entity.getNom(),
                entity.getNiveau(),
                entity.getQuantiteUniteBase(),
                entity.isEstUniteBase(),
                entity.getPrixAchat(),
                entity.getPrixVente());
    }
}
