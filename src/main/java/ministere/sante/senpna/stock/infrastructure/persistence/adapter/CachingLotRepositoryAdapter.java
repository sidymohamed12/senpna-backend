package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;
import ministere.sante.senpna.stock.domain.criteria.AlertePeremptionCriteria;
import ministere.sante.senpna.stock.domain.criteria.LotSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.infrastructure.persistence.cache.LotCacheEntry;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Décorateur de cache Redis clé/valeur pour {@link LotRepositoryPort}.
 *
 * <p>
 * Seule la lecture unitaire ({@code findById}, clé {@code lot:id:<id>})
 * est mise en cache — jamais au démarrage, uniquement à la demande.
 * {@code findActifsNonExpiresParMedicamentTriesFefo} (allocation FEFO) et
 * {@code findActifsExpires} (job d'expiration) doivent toujours refléter
 * l'état exact de la base : les mettre en cache risquerait de faire
 * expédier un lot déjà bloqué entre-temps par un autre acteur. Même
 * logique pour {@code search} (trop de combinaisons de filtres pour un
 * bénéfice réel).
 * </p>
 */
@Component
@Primary
public class CachingLotRepositoryAdapter implements LotRepositoryPort {

    private static final String PREFIX = "lot:id:";

    private final LotRepositoryAdapter delegate;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public CachingLotRepositoryAdapter(LotRepositoryAdapter delegate, JsonCacheSupport cache,
            AppProperties appProperties) {
        this.delegate = delegate;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    public Optional<Lot> findById(LotId id) {
        String key = cleDe(id);

        Optional<Lot> enCache = cache.get(key, LotCacheEntry.class).map(LotCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<Lot> depuisBase = delegate.findById(id);
        depuisBase.ifPresent(lot -> mettreEnCache(key, lot));
        return depuisBase;
    }

    @Override
    public boolean existsByMedicamentIdAndNumeroLotIgnoreCase(MedicamentId medicamentId, String numeroLot) {
        return delegate.existsByMedicamentIdAndNumeroLotIgnoreCase(medicamentId, numeroLot);
    }

    @Override
    public Lot save(Lot lot) {
        Lot sauvegarde = delegate.save(lot);
        mettreEnCache(cleDe(sauvegarde.getId()), sauvegarde);
        return sauvegarde;
    }

    @Override
    public PageResult<Lot> search(LotSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }

    @Override
    public List<Lot> findActifsNonExpiresParMedicamentTriesFefo(MedicamentId medicamentId) {
        return delegate.findActifsNonExpiresParMedicamentTriesFefo(medicamentId);
    }

    @Override
    public PageResult<Lot> findExpirantAvant(AlertePeremptionCriteria criteria, PageRequest pageRequest) {
        return delegate.findExpirantAvant(criteria, pageRequest);
    }

    @Override
    public List<Lot> findActifsExpires() {
        return delegate.findActifsExpires();
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private void mettreEnCache(String key, Lot lot) {
        cache.put(key, LotCacheEntry.from(lot), appProperties.cache().lotTtl());
    }

    private String cleDe(LotId id) {
        return PREFIX + id.getValue();
    }
}
