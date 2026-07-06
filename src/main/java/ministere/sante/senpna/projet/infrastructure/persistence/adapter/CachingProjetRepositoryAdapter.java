package ministere.sante.senpna.projet.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.projet.domain.criteria.ProjetSearchCriteria;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.infrastructure.persistence.cache.ProjetCacheEntry;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Décorateur de cache Redis clé/valeur pour {@link ProjetRepositoryPort} —
 * symétrique à {@code CachingMedicamentRepositoryAdapter} : chaque projet
 * est mis en cache <strong>à la demande</strong>, à sa propre clé
 * ({@code projet:id:<id>}), lors de sa première lecture — jamais au
 * démarrage de l'application (contrairement aux référentiels
 * quasi-statiques préchargés en mémoire, cf.
 * {@code shared/infrastructure/cache}).
 *
 * <h3>Stratégie</h3>
 * <ul>
 * <li>{@code findById} : cache-aside — lecture cache, puis base si absent,
 * avec repeuplement du cache ;</li>
 * <li>{@code save} : écriture base puis mise à jour immédiate de la clé de
 * cache (évite un miss inutile juste après écriture — utile en particulier
 * après {@code publier}/{@code archiver}/{@code désactiver}, qui passent
 * tous par {@code save});</li>
 * <li>{@code search} : jamais mis en cache — trop de combinaisons de clés
 * possibles pour un bénéfice réel.</li>
 * </ul>
 *
 * <p>
 * Implémente directement {@link ProjetRepositoryPort} et est marqué
 * {@link Primary} : les use cases (couche application), qui ne dépendent
 * que du port, bénéficient du cache de façon totalement transparente, sans
 * le savoir — {@link ProjetRepositoryAdapter} (accès JPA) reste injecté ici
 * par son type concret, jamais par l'interface, pour éviter toute
 * ambiguïté Spring entre les deux beans.
 * </p>
 */
@Component
@Primary
public class CachingProjetRepositoryAdapter implements ProjetRepositoryPort {

    private static final String PREFIX = "projet:id:";

    private final ProjetRepositoryAdapter delegate;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public CachingProjetRepositoryAdapter(ProjetRepositoryAdapter delegate, JsonCacheSupport cache,
            AppProperties appProperties) {
        this.delegate = delegate;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    public Optional<Projet> findById(ProjetId id) {
        String key = cleDe(id);

        Optional<Projet> enCache = cache.get(key, ProjetCacheEntry.class).map(ProjetCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<Projet> depuisBase = delegate.findById(id);
        depuisBase.ifPresent(projet -> mettreEnCache(key, projet));
        return depuisBase;
    }

    @Override
    public Projet save(Projet projet) {
        Projet sauvegarde = delegate.save(projet);
        mettreEnCache(cleDe(sauvegarde.getId()), sauvegarde);
        return sauvegarde;
    }

    @Override
    public PageResult<Projet> search(ProjetSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private void mettreEnCache(String key, Projet projet) {
        cache.put(key, ProjetCacheEntry.from(projet), appProperties.cache().projetTtl());
    }

    private String cleDe(ProjetId id) {
        return PREFIX + id.getValue();
    }
}
