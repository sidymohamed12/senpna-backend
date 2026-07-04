package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Résolution de fournisseurs à partir d'un cache en mémoire — distinct de
 * {@link FournisseurQueryPort}, qui interroge la source de vérité (base
 * de données). Permet à n'importe quelle feature (notamment
 * {@code catalogue}, pour afficher le nom du fournisseur d'un lot sans
 * aller-retour SQL par ligne) de résoudre un fournisseur sans dépendre du
 * module {@code fournisseur} — même stratégie que {@link RoleCachePort}
 * / {@link RegionCachePort}.
 */
public interface FournisseurCachePort {

    Optional<FournisseurProjection> findById(UUID id);

    Set<FournisseurProjection> findAllById(Set<UUID> ids);

    /**
     * Recharge intégralement le cache depuis la source de vérité — à
     * appeler après toute création/modification d'un fournisseur.
     */
    void reload();
}
