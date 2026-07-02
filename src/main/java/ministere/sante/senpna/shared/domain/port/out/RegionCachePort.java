package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Résolution de régions à partir d'un cache en mémoire — distinct de
 * {@link RegionQueryPort}, qui interroge la source de vérité (base de
 * données). Permet à n'importe quelle feature de résoudre une région
 * (existence, nom d'affichage) sans dépendre directement du module
 * {@code organisation}, ni générer d'aller-retour SQL — même stratégie
 * que {@link RoleCachePort} pour les rôles.
 */
public interface RegionCachePort {

    Optional<RegionProjection> findById(UUID id);

    boolean existsById(UUID id);

    Set<RegionProjection> findAllById(Set<UUID> ids);

    /**
     * Recharge intégralement le cache depuis la source de vérité.
     *
     * <p>
     * Contrairement aux rôles (donnée figée, provisionnée uniquement par
     * migration), les régions sont créées via API : tout use case qui crée
     * ou modifie une région doit appeler cette méthode après persistance
     * pour éviter de servir une vue obsolète le temps qu'un rechargement
     * périodique n'intervienne.
     * </p>
     */
    void reload();
}
