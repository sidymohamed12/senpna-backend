package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;
import ministere.sante.senpna.stock.domain.criteria.MouvementSearchCriteria;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;
import ministere.sante.senpna.stock.infrastructure.persistence.cache.MouvementStockCacheEntry;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Décorateur de cache Redis clé/valeur pour
 * {@link MouvementStockRepositoryPort} — mis en cache à la demande
 * (jamais au démarrage), clé {@code mouvement:id:<id>}.
 *
 * <p>
 * Le journal des mouvements étant <em>append-only</em> (cf. Javadoc du
 * port), il n'y a structurellement aucune éviction à gérer : un mouvement
 * ne change jamais une fois créé. {@code save} — qui ne fait
 * qu'insérer — repeuple directement le cache avec la valeur fraîchement
 * créée plutôt que de l'évincer, épargnant une lecture base immédiate si
 * le mouvement est relu juste après (ex : réponse d'API qui ré-affiche le
 * mouvement créé).
 * </p>
 */
@Component
@Primary
public class CachingMouvementStockRepositoryAdapter implements MouvementStockRepositoryPort {

    private static final String PREFIX = "mouvement:id:";

    private final MouvementStockRepositoryAdapter delegate;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public CachingMouvementStockRepositoryAdapter(MouvementStockRepositoryAdapter delegate, JsonCacheSupport cache,
            AppProperties appProperties) {
        this.delegate = delegate;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    public Optional<MouvementStock> findById(MouvementStockId id) {
        String key = cleDe(id);

        Optional<MouvementStock> enCache = cache.get(key, MouvementStockCacheEntry.class)
                .map(MouvementStockCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<MouvementStock> depuisBase = delegate.findById(id);
        depuisBase.ifPresent(mouvement -> mettreEnCache(key, mouvement));
        return depuisBase;
    }

    @Override
    public MouvementStock save(MouvementStock mouvementStock) {
        MouvementStock sauvegarde = delegate.save(mouvementStock);
        mettreEnCache(cleDe(sauvegarde.getId()), sauvegarde);
        return sauvegarde;
    }

    @Override
    public PageResult<MouvementStock> search(MouvementSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private void mettreEnCache(String key, MouvementStock mouvement) {
        cache.put(key, MouvementStockCacheEntry.from(mouvement), appProperties.cache().mouvementTtl());
    }

    private String cleDe(MouvementStockId id) {
        return PREFIX + id.getValue();
    }
}
