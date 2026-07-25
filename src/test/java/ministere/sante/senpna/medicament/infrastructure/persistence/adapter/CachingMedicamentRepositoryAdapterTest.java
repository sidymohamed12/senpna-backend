package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.config.AppProperties.CacheProperties;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;
import ministere.sante.senpna.medicament.infrastructure.persistence.cache.MedicamentCacheEntry;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CachingMedicamentRepositoryAdapter — cache-aside Redis pour les médicaments")
class CachingMedicamentRepositoryAdapterTest {

    @Mock
    MedicamentRepositoryAdapter delegate;
    @Mock
    JsonCacheSupport cache;

    CachingMedicamentRepositoryAdapter sut;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(null, null, null, null,
                new CacheProperties(Duration.ofHours(12), null, null, null, null, null, null, null));
        sut = new CachingMedicamentRepositoryAdapter(delegate, cache, appProperties);
    }

    private Medicament medicament() {
        return Medicament.creer(new Medicament.CreationCommand("MED-1", "Zolpidem", "Zolpidem", "10mg", FormeId.generate(), FamilleId.generate(),
                VoieAdministration.ORALE, null, null, null, false, "Sanofi", null, null));
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("cache hit → renvoyé directement, jamais interrogé en base")
        void cacheHit_pasAccesBase() {
            Medicament m = medicament();
            when(cache.get(anyString(), eq(MedicamentCacheEntry.class)))
                    .thenReturn(Optional.of(MedicamentCacheEntry.from(m)));

            Optional<Medicament> result = sut.findById(m.getId());

            assertThat(result).isPresent();
            verify(delegate, never()).findById(any());
        }

        @Test
        @DisplayName("cache miss, trouvé en base → repeuple le cache avec le TTL médicament")
        void cacheMiss_repeupleCache() {
            Medicament m = medicament();
            when(cache.get(anyString(), eq(MedicamentCacheEntry.class))).thenReturn(Optional.empty());
            when(delegate.findById(m.getId())).thenReturn(Optional.of(m));

            sut.findById(m.getId());

            verify(cache).put(anyString(), any(MedicamentCacheEntry.class), eq(Duration.ofHours(12)));
        }
    }

    @Nested
    @DisplayName("existsByCodeIgnoreCase() / search()")
    class ExistsEtSearch {

        @Test
        @DisplayName("existsByCodeIgnoreCase() délègue directement")
        void existsByCodeIgnoreCase_delegue() {
            when(delegate.existsByCodeIgnoreCase("MED-1")).thenReturn(true);

            assertThat(sut.existsByCodeIgnoreCase("MED-1")).isTrue();
        }

        @Test
        @DisplayName("search() délègue toujours à la base, jamais mis en cache")
        void search_delegueToujours() {
            when(delegate.search(any(), any())).thenReturn(
                    ministere.sante.senpna.shared.domain.valueobject.PageResult.of(java.util.List.of(), 0, 20, 0));

            sut.search(null, ministere.sante.senpna.shared.domain.valueobject.PageRequest.of(0, 20, null, null));

            verify(cache, never()).get(anyString(), any());
        }
    }
}
