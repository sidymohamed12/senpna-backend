package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.medicament.domain.criteria.MedicamentSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.cache.MedicamentCacheEntry;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Décorateur de cache Redis clé/valeur pour {@link MedicamentRepositoryPort}
 * — contrairement aux référentiels quasi-statiques ({@code Region},
 * {@code Fournisseur}, {@code Forme}, {@code Famille}), le référentiel
 * médicament est trop volumineux (potentiellement plusieurs milliers de
 * lignes, cf. Javadoc {@code CatalogueEntryAssembler}) pour être
 * intégralement chargé en mémoire au démarrage : chaque médicament est mis
 * en cache <strong>à la demande</strong>, à sa propre clé
 * ({@code medicament:id:<id>}), lors de sa première lecture — jamais au
 * démarrage de l'application.
 *
 * <h3>Stratégie</h3>
 * <ul>
 * <li>{@code findById} : cache-aside — lecture cache, puis base si absent,
 * avec repeuplement du cache ;</li>
 * <li>{@code save} : écriture base puis mise à jour immédiate de la clé de
 * cache (évite un miss inutile juste après écriture) ;</li>
 * <li>{@code existsByCodeIgnoreCase(*)} et {@code search} : jamais mis en
 * cache — trop de combinaisons de clés possibles pour un bénéfice
 * réel.</li>
 * </ul>
 *
 * <p>
 * Implémente directement {@link MedicamentRepositoryPort} et est marqué
 * {@link Primary} : les use cases (couche application), qui ne dépendent
 * que du port, bénéficient du cache de façon totalement transparente,
 * sans le savoir — {@link MedicamentRepositoryAdapter} (accès JPA) reste
 * injecté ici par son type concret, jamais par l'interface, pour éviter
 * toute ambiguïté Spring entre les deux beans.
 * </p>
 */
@Component
@Primary
public class CachingMedicamentRepositoryAdapter implements MedicamentRepositoryPort {

    private static final String PREFIX = "medicament:id:";

    private final MedicamentRepositoryAdapter delegate;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public CachingMedicamentRepositoryAdapter(MedicamentRepositoryAdapter delegate, JsonCacheSupport cache,
            AppProperties appProperties) {
        this.delegate = delegate;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    public Optional<Medicament> findById(MedicamentId id) {
        String key = cleDe(id);

        Optional<Medicament> enCache = cache.get(key, MedicamentCacheEntry.class).map(MedicamentCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<Medicament> depuisBase = delegate.findById(id);
        depuisBase.ifPresent(medicament -> mettreEnCache(key, medicament));
        return depuisBase;
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code) {
        return delegate.existsByCodeIgnoreCase(code);
    }

    @Override
    public boolean existsByCodeIgnoreCaseAndIdNot(String code, MedicamentId id) {
        return delegate.existsByCodeIgnoreCaseAndIdNot(code, id);
    }

    @Override
    public Medicament save(Medicament medicament) {
        Medicament sauvegarde = delegate.save(medicament);
        mettreEnCache(cleDe(sauvegarde.getId()), sauvegarde);
        return sauvegarde;
    }

    @Override
    public PageResult<Medicament> search(MedicamentSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private void mettreEnCache(String key, Medicament medicament) {
        cache.put(key, MedicamentCacheEntry.from(medicament), appProperties.cache().medicamentTtl());
    }

    private String cleDe(MedicamentId id) {
        return PREFIX + id.getValue();
    }
}
