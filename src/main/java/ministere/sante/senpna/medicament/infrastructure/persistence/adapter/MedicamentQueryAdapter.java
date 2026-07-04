package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FamilleJpaRepository;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.MedicamentJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du port {@code shared} {@link MedicamentQueryPort} — le
 * module {@code medicament} reste le seul propriétaire des agrégats
 * {@code Medicament}/{@code Famille} et de leur persistance ; les autres
 * features (notamment {@code catalogue}) n'accèdent qu'à cette projection
 * en lecture, déjà enrichie du libellé de famille thérapeutique.
 * Symétrique à {@code EntrepotQueryAdapter} / {@code RegionQueryAdapter}.
 */
@Component
@Transactional(readOnly = true)
public class MedicamentQueryAdapter implements MedicamentQueryPort {

    private final MedicamentJpaRepository medicamentJpaRepository;
    private final FamilleJpaRepository familleJpaRepository;

    public MedicamentQueryAdapter(MedicamentJpaRepository medicamentJpaRepository,
            FamilleJpaRepository familleJpaRepository) {
        this.medicamentJpaRepository = medicamentJpaRepository;
        this.familleJpaRepository = familleJpaRepository;
    }

    @Override
    public List<MedicamentProjection> findAllById(Collection<UUID> ids) {
        List<MedicamentJpaEntity> medicaments = medicamentJpaRepository.findAllById(ids);

        List<UUID> familleIds = medicaments.stream().map(MedicamentJpaEntity::getFamilleId).distinct().toList();
        Map<UUID, String> libellesParFamilleId = familleJpaRepository.findAllById(familleIds).stream()
                .collect(Collectors.toMap(FamilleJpaEntity::getId, FamilleJpaEntity::getLibelle));

        return medicaments.stream().map(entity -> toProjection(entity, libellesParFamilleId)).toList();
    }

    private MedicamentProjection toProjection(MedicamentJpaEntity entity, Map<UUID, String> libellesParFamilleId) {
        return new MedicamentProjection(
                entity.getId(),
                entity.getCode(),
                entity.getNomCommercial(),
                entity.getDci(),
                libellesParFamilleId.get(entity.getFamilleId()),
                entity.getFabricant(),
                entity.isActif());
    }
}
