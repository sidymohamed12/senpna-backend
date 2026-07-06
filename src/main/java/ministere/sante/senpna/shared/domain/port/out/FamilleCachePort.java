package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.FamilleProjection;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Résolution de familles thérapeutiques à partir d'un cache en mémoire —
 * distinct de {@link FamilleQueryPort}, qui interroge la source de vérité
 * (base de données). Même stratégie que {@link FormeCachePort} /
 * {@link RegionCachePort} / {@link FournisseurCachePort}.
 */
public interface FamilleCachePort {

    Optional<FamilleProjection> findById(UUID id);

    boolean existsById(UUID id);

    Set<FamilleProjection> findAllById(Set<UUID> ids);

    /**
     * Recharge intégralement le cache depuis la source de vérité — à
     * appeler après toute création/modification/archivage d'une famille.
     */
    void reload();
}
