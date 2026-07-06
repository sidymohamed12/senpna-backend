package ministere.sante.senpna.actualite.infrastructure.persistence.adapter;

import ministere.sante.senpna.actualite.domain.criteria.ActualiteSearchCriteria;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.infrastructure.persistence.cache.ActualiteCacheEntry;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Décorateur de cache Redis clé/valeur pour {@link ActualiteRepositoryPort}
 * — symétrique à {@code CachingMedicamentRepositoryAdapter} : chaque
 * actualité est mise en cache <strong>à la demande</strong>, à sa propre
 * clé ({@code actualite:id:<id>}), lors de sa première lecture — jamais au
 * démarrage de l'application.
 *
 * <h3>Stratégie</h3>
 * <ul>
 * <li>{@code findById} : cache-aside — lecture cache, puis base si absent,
 * avec repeuplement du cache ;</li>
 * <li>{@code save} : écriture base puis mise à jour immédiate de la clé de
 * cache (évite un miss inutile juste après écriture — utile en particulier
 * après {@code publier}/{@code désactiver}/{@code remettreEnBrouillon},
 * qui passent tous par {@code save});</li>
 * <li>{@code search} : jamais mis en cache — trop de combinaisons de clés
 * possibles pour un bénéfice réel.</li>
 * </ul>
 *
 * <p>
 * Implémente directement {@link ActualiteRepositoryPort} et est marqué
 * {@link Primary} : les use cases (couche application), qui ne dépendent
 * que du port, bénéficient du cache de façon totalement transparente, sans
 * le savoir — {@link ActualiteRepositoryAdapter} (accès JPA) reste injecté
 * ici par son type concret, jamais par l'interface, pour éviter toute
 * ambiguïté Spring entre les deux beans.
 * </p>
 */
@Component
@Primary
public class CachingActualiteRepositoryAdapter implements ActualiteRepositoryPort {

    private static final String PREFIX = "actualite:id:";

    private final ActualiteRepositoryAdapter delegate;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public CachingActualiteRepositoryAdapter(ActualiteRepositoryAdapter delegate, JsonCacheSupport cache,
            AppProperties appProperties) {
        this.delegate = delegate;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    public Optional<Actualite> findById(ActualiteId id) {
        String key = cleDe(id);

        Optional<Actualite> enCache = cache.get(key, ActualiteCacheEntry.class).map(ActualiteCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<Actualite> depuisBase = delegate.findById(id);
        depuisBase.ifPresent(actualite -> mettreEnCache(key, actualite));
        return depuisBase;
    }

    @Override
    public Actualite save(Actualite actualite) {
        Actualite sauvegarde = delegate.save(actualite);
        mettreEnCache(cleDe(sauvegarde.getId()), sauvegarde);
        return sauvegarde;
    }

    @Override
    public PageResult<Actualite> search(ActualiteSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private void mettreEnCache(String key, Actualite actualite) {
        cache.put(key, ActualiteCacheEntry.from(actualite), appProperties.cache().actualiteTtl());
    }

    private String cleDe(ActualiteId id) {
        return PREFIX + id.getValue();
    }
}
