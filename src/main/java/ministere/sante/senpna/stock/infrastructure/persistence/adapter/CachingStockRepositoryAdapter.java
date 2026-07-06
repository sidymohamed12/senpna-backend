package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;
import ministere.sante.senpna.stock.infrastructure.persistence.cache.StockCacheEntry;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Décorateur de cache Redis clé/valeur pour {@link StockRepositoryPort}.
 *
 * <p>
 * Mis en cache : {@code findById} (clé {@code stock:id:<id>}) et
 * {@code findByEntrepotIdAndLotId} (clé
 * {@code stock:entrepot:<entrepotId>:lot:<lotId>}) — lectures « simples »
 * utilisées pour l'affichage (ex: {@code GetStockUseCase}).
 * </p>
 * <p>
 * <strong>Jamais</strong> mis en cache :
 * {@code findByEntrepotIdAndLotIdForUpdate} — verrou pessimiste
 * (SELECT ... FOR UPDATE) utilisé pendant réservation/entrée/sortie ;
 * servir une valeur de cache à cet endroit contournerait le verrou et
 * ouvrirait une condition de course sur les quantités. TTL volontairement
 * court ({@code app.cache.stock-ttl}, 2 min par défaut) et éviction
 * immédiate à chaque écriture pour limiter la fenêtre d'incohérence sur
 * les lectures non verrouillantes.
 * </p>
 */
@Component
@Primary
public class CachingStockRepositoryAdapter implements StockRepositoryPort {

    private static final String PREFIX_ID = "stock:id:";
    private static final String PREFIX_ENTREPOT_LOT = "stock:entrepot:";

    private final StockRepositoryAdapter delegate;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public CachingStockRepositoryAdapter(StockRepositoryAdapter delegate, JsonCacheSupport cache,
            AppProperties appProperties) {
        this.delegate = delegate;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    public Optional<Stock> findById(StockId id) {
        String key = cleId(id);

        Optional<Stock> enCache = cache.get(key, StockCacheEntry.class).map(StockCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<Stock> depuisBase = delegate.findById(id);
        depuisBase.ifPresent(this::mettreEnCache);
        return depuisBase;
    }

    @Override
    public Optional<Stock> findByEntrepotIdAndLotId(EntrepotId entrepotId, LotId lotId) {
        String key = cleEntrepotLot(entrepotId, lotId);

        Optional<Stock> enCache = cache.get(key, StockCacheEntry.class).map(StockCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<Stock> depuisBase = delegate.findByEntrepotIdAndLotId(entrepotId, lotId);
        depuisBase.ifPresent(this::mettreEnCache);
        return depuisBase;
    }

    /**
     * Verrou pessimiste — bypass total du cache, toujours lu en base.
     */
    @Override
    public Optional<Stock> findByEntrepotIdAndLotIdForUpdate(EntrepotId entrepotId, LotId lotId) {
        return delegate.findByEntrepotIdAndLotIdForUpdate(entrepotId, lotId);
    }

    @Override
    public Stock save(Stock stock) {
        Stock sauvegarde = delegate.save(stock);
        mettreEnCache(sauvegarde);
        return sauvegarde;
    }

    @Override
    public PageResult<Stock> search(StockSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private void mettreEnCache(Stock stock) {
        StockCacheEntry entry = StockCacheEntry.from(stock);
        cache.put(cleId(stock.getId()), entry, appProperties.cache().stockTtl());
        cache.put(cleEntrepotLot(stock.getEntrepotId(), stock.getLotId()), entry, appProperties.cache().stockTtl());
    }

    private String cleId(StockId id) {
        return PREFIX_ID + id.getValue();
    }

    private String cleEntrepotLot(EntrepotId entrepotId, LotId lotId) {
        return PREFIX_ENTREPOT_LOT + entrepotId.getValue() + ":lot:" + lotId.getValue();
    }
}
