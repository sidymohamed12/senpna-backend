package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.FormeProjection;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Résolution de formes pharmaceutiques à partir d'un cache en mémoire —
 * distinct de {@link FormeQueryPort}, qui interroge la source de vérité
 * (base de données). Permet à n'importe quelle feature de résoudre une
 * forme (existence, libellé d'affichage) sans dépendre directement du
 * module {@code medicament}, ni générer d'aller-retour SQL — même
 * stratégie que {@link RegionCachePort} / {@link FournisseurCachePort}
 * pour les régions/fournisseurs.
 */
public interface FormeCachePort {

    Optional<FormeProjection> findById(UUID id);

    boolean existsById(UUID id);

    Set<FormeProjection> findAllById(Set<UUID> ids);

    /**
     * Recharge intégralement le cache depuis la source de vérité.
     *
     * <p>
     * Les formes sont créées/modifiées/archivées via API : tout use case
     * qui écrit une forme doit appeler cette méthode après persistance
     * pour éviter de servir une vue obsolète.
     * </p>
     */
    void reload();
}
