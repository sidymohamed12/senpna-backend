package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.persistence.cache.UserCacheEntry;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.config.AppProperties.CacheProperties;
import ministere.sante.senpna.shared.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CachingUserManagementRepositoryAdapter — cache-aside Redis pour les utilisateurs")
class CachingUserManagementRepositoryAdapterTest {

    @Mock
    UserManagementRepositoryAdapter delegate;
    @Mock
    JsonCacheSupport cache;

    CachingUserManagementRepositoryAdapter sut;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(null, null, null, null,
                new CacheProperties(null, null, null, null, Duration.ofHours(5), null, null, null));
        sut = new CachingUserManagementRepositoryAdapter(delegate, cache, appProperties);
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("cache hit → renvoyé directement, la base n'est jamais interrogée")
        void cacheHit_pasAccesBase() {
            User user = UserFixtures.actif();
            when(cache.get(anyString(), eq(UserCacheEntry.class)))
                    .thenReturn(Optional.of(UserCacheEntry.from(user)));

            Optional<User> result = sut.findById(UserId.of(UserFixtures.USER_ID));

            assertThat(result).isPresent();
            verify(delegate, never()).findById(any());
        }

        @Test
        @DisplayName("cache miss, trouvé en base → repeuple le cache")
        void cacheMiss_repeupleCache() {
            User user = UserFixtures.actif();
            when(cache.get(anyString(), eq(UserCacheEntry.class))).thenReturn(Optional.empty());
            when(delegate.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));

            sut.findById(UserId.of(UserFixtures.USER_ID));

            verify(cache).put(eq("user:id:" + UserFixtures.USER_ID), any(UserCacheEntry.class),
                    eq(Duration.ofHours(5)));
        }
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("sauvegarde en base puis met immédiatement à jour le cache — garantit la visibilité"
                + " immédiate d'un verrouillage")
        void sauvegardeEtMetAJourCache() {
            User user = UserFixtures.verrouille();
            when(delegate.save(user)).thenReturn(user);

            sut.save(user);

            verify(cache).put(eq("user:id:" + UserFixtures.USER_ID), any(UserCacheEntry.class),
                    eq(Duration.ofHours(5)));
        }
    }

    @Nested
    @DisplayName("search() / existsByEmail()")
    class SearchEtExists {

        @Test
        @DisplayName("search() délègue toujours à la base, jamais mis en cache")
        void search_delegueToujoursALaBase() {
            when(delegate.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            sut.search(UserSearchCriteria.vide(), PageRequest.of(0, 20, null, null));

            verify(cache, never()).get(anyString(), any());
        }

        @Test
        @DisplayName("existsByEmail() délègue directement")
        void existsByEmail_delegue() {
            when(delegate.existsByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(true);

            assertThat(sut.existsByEmail(Email.of(UserFixtures.EMAIL))).isTrue();
        }
    }
}
