package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.infrastructure.persistence.cache.UserCacheEntry;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Décorateur de cache Redis clé/valeur pour
 * {@link UserManagementRepositoryPort} — mis en cache à la demande
 * (jamais au démarrage — contrairement aux rôles, quasi-statiques et
 * provisionnés uniquement par migration, cf. {@code RoleCache}), clé
 * {@code user:id:<id>}.
 *
 * <p>
 * {@code findById} est appelé à haute fréquence (chaque requête
 * authentifiée résout l'acteur courant, cf. {@code CurrentUser} /
 * {@code UserHierarchyGuard} / {@code CatalogueAccessGuard}...) : le
 * mettre en cache réduit directement la charge SQL sur le chemin critique
 * de chaque requête. {@code save} — invoqué à chaque connexion échouée
 * (compteur de tentatives), verrouillage, changement de mot de passe,
 * activation/désactivation — évince et repeuple systématiquement la
 * clé, garantissant qu'un verrouillage de compte ne peut jamais rester
 * masqué par une entrée de cache périmée.
 * </p>
 */
@Component
@Primary
public class CachingUserManagementRepositoryAdapter implements UserManagementRepositoryPort {

    private static final String PREFIX = "user:id:";

    private final UserManagementRepositoryAdapter delegate;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public CachingUserManagementRepositoryAdapter(UserManagementRepositoryAdapter delegate, JsonCacheSupport cache,
            AppProperties appProperties) {
        this.delegate = delegate;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    public Optional<User> findById(UserId id) {
        String key = cleDe(id);

        Optional<User> enCache = cache.get(key, UserCacheEntry.class).map(UserCacheEntry::toDomain);
        if (enCache.isPresent()) {
            return enCache;
        }

        Optional<User> depuisBase = delegate.findById(id);
        depuisBase.ifPresent(user -> mettreEnCache(key, user));
        return depuisBase;
    }

    @Override
    public PageResult<User> search(UserSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }

    @Override
    public User save(User user) {
        User sauvegarde = delegate.save(user);
        mettreEnCache(cleDe(sauvegarde.getId()), sauvegarde);
        return sauvegarde;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return delegate.existsByEmail(email);
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private void mettreEnCache(String key, User user) {
        cache.put(key, UserCacheEntry.from(user), appProperties.cache().userTtl());
    }

    private String cleDe(UserId id) {
        return PREFIX + id.getValue();
    }
}
