package ministere.sante.senpna.projet.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.config.AppProperties.CacheProperties;
import ministere.sante.senpna.projet.domain.criteria.ProjetSearchCriteria;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.infrastructure.persistence.cache.ProjetCacheEntry;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
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
@DisplayName("CachingProjetRepositoryAdapter — cache-aside Redis pour les projets")
class CachingProjetRepositoryAdapterTest {

    @Mock
    ProjetRepositoryAdapter delegate;
    @Mock
    JsonCacheSupport cache;

    CachingProjetRepositoryAdapter sut;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(null, null, null, null, null,
                new CacheProperties(null, null, null, null, null, null, Duration.ofHours(24), null));
        sut = new CachingProjetRepositoryAdapter(delegate, cache, appProperties);
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("cache hit → renvoyé directement, la base n'est jamais interrogée")
        void cacheHit_pasAccesBase() {
            Projet projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Nom", "Desc", List.of(), List.of(), null));
            ProjetCacheEntry entry = ProjetCacheEntry.from(projet);
            when(cache.get(anyString(), eq(ProjetCacheEntry.class))).thenReturn(Optional.of(entry));

            Optional<Projet> result = sut.findById(projet.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getNom()).isEqualTo("Nom");
            verify(delegate, never()).findById(any());
        }

        @Test
        @DisplayName("cache miss, trouvé en base → repeuple le cache")
        void cacheMiss_repeupleCache() {
            Projet projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Nom", "Desc", List.of(), List.of(), null));
            when(cache.get(anyString(), eq(ProjetCacheEntry.class))).thenReturn(Optional.empty());
            when(delegate.findById(projet.getId())).thenReturn(Optional.of(projet));

            Optional<Projet> result = sut.findById(projet.getId());

            assertThat(result).isPresent();
            verify(cache).put(eq("projet:id:" + projet.getId().getValue()), any(ProjetCacheEntry.class),
                    eq(Duration.ofHours(24)));
        }

        @Test
        @DisplayName("cache miss, introuvable en base → vide, aucune écriture cache")
        void cacheMissEtBaseVide_pasEcritureCache() {
            when(cache.get(anyString(), eq(ProjetCacheEntry.class))).thenReturn(Optional.empty());
            Projet projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Nom", "Desc", List.of(), List.of(), null));
            when(delegate.findById(projet.getId())).thenReturn(Optional.empty());

            assertThat(sut.findById(projet.getId())).isEmpty();
            verify(cache, never()).put(anyString(), any(), any());
        }
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("sauvegarde en base puis met immédiatement à jour le cache")
        void sauvegardeEtMetAJourCache() {
            Projet projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Nom", "Desc", List.of(), List.of(), null));
            when(delegate.save(projet)).thenReturn(projet);

            Projet result = sut.save(projet);

            assertThat(result).isSameAs(projet);
            verify(cache).put(eq("projet:id:" + projet.getId().getValue()), any(ProjetCacheEntry.class),
                    eq(Duration.ofHours(24)));
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("délègue toujours à la base, jamais mis en cache")
        void delegueToujoursALaBase() {
            when(delegate.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            sut.search(ProjetSearchCriteria.vide(), PageRequest.of(0, 20, null, null));

            verify(delegate).search(any(), any());
            verify(cache, never()).get(anyString(), any());
        }
    }
}
