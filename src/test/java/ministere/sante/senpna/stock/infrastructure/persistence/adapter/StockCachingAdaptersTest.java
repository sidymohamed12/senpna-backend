package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.config.AppProperties.CacheProperties;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.stock.infrastructure.persistence.cache.LotCacheEntry;
import ministere.sante.senpna.stock.infrastructure.persistence.cache.MouvementStockCacheEntry;
import ministere.sante.senpna.stock.infrastructure.persistence.cache.StockCacheEntry;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Adaptateurs de cache-aside Redis du module stock")
class StockCachingAdaptersTest {

    @Mock
    JsonCacheSupport cache;

    private AppProperties appProperties(Duration medicamentTtl, Duration lotTtl, Duration stockTtl,
            Duration mouvementTtl) {
        return new AppProperties(null, null, null, null, null,
                new CacheProperties(medicamentTtl, lotTtl, stockTtl, mouvementTtl, null, null, null, null));
    }

    @Nested
    @DisplayName("CachingLotRepositoryAdapter")
    class LotCaching {

        @Mock
        LotRepositoryAdapter delegate;
        CachingLotRepositoryAdapter sut;

        @BeforeEach
        void setUp() {
            sut = new CachingLotRepositoryAdapter(delegate, cache,
                    appProperties(null, Duration.ofHours(6), null, null));
        }

        @Test
        @DisplayName("cache hit → renvoyé directement, jamais interrogé en base")
        void cacheHit_pasAccesBase() {
            Lot lot = Lot.creer("L1", MedicamentId.generate(), FournisseurId.generate(),
                    LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN);
            when(cache.get(anyString(), eq(LotCacheEntry.class))).thenReturn(Optional.of(LotCacheEntry.from(lot)));

            Optional<Lot> result = sut.findById(lot.getId());

            assertThat(result).isPresent();
            verify(delegate, never()).findById(any());
        }

        @Test
        @DisplayName("cache miss, trouvé en base → repeuple le cache avec le TTL lot")
        void cacheMiss_repeupleCache() {
            Lot lot = Lot.creer("L1", MedicamentId.generate(), FournisseurId.generate(),
                    LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN);
            when(cache.get(anyString(), eq(LotCacheEntry.class))).thenReturn(Optional.empty());
            when(delegate.findById(lot.getId())).thenReturn(Optional.of(lot));

            sut.findById(lot.getId());

            verify(cache).put(anyString(), any(LotCacheEntry.class), eq(Duration.ofHours(6)));
        }

        @Test
        @DisplayName("existsByMedicamentIdAndNumeroLotIgnoreCase() délègue directement, jamais mis en cache")
        void existsByMedicamentIdEtNumero_delegue() {
            when(delegate.existsByMedicamentIdAndNumeroLotIgnoreCase(any(), any())).thenReturn(true);

            assertThat(sut.existsByMedicamentIdAndNumeroLotIgnoreCase(MedicamentId.generate(), "L1")).isTrue();
        }
    }

    @Nested
    @DisplayName("CachingStockRepositoryAdapter")
    class StockCaching {

        @Mock
        StockRepositoryAdapter delegate;
        CachingStockRepositoryAdapter sut;

        @BeforeEach
        void setUp() {
            sut = new CachingStockRepositoryAdapter(delegate, cache,
                    appProperties(null, null, Duration.ofMinutes(30), null));
        }

        @Test
        @DisplayName("save() met en cache SOUS DEUX CLÉS : par id et par (entrepôt, lot)")
        void save_metEnCacheSousDeuxCles() {
            Stock stock = Stock.ouvrir(EntrepotId.generate(), LotId.generate(), MedicamentId.generate(), null);
            when(delegate.save(stock)).thenReturn(stock);

            sut.save(stock);

            verify(cache, times(2)).put(anyString(), any(StockCacheEntry.class), eq(Duration.ofMinutes(30)));
        }

        @Test
        @DisplayName("findByEntrepotIdAndLotIdForUpdate() bypass totalement le cache (verrou pessimiste)")
        void findForUpdate_bypassCache() {
            EntrepotId entrepotId = EntrepotId.generate();
            LotId lotId = LotId.generate();
            when(delegate.findByEntrepotIdAndLotIdForUpdate(entrepotId, lotId)).thenReturn(Optional.empty());

            sut.findByEntrepotIdAndLotIdForUpdate(entrepotId, lotId);

            verify(cache, never()).get(anyString(), any());
            verify(delegate).findByEntrepotIdAndLotIdForUpdate(entrepotId, lotId);
        }

        @Test
        @DisplayName("search() délègue toujours à la base")
        void search_delegueToujours() {
            when(delegate.search(any(), any())).thenReturn(
                    ministere.sante.senpna.shared.domain.valueobject.PageResult.of(java.util.List.of(), 0, 20, 0));

            sut.search(null, ministere.sante.senpna.shared.domain.valueobject.PageRequest.of(0, 20, null, null));

            verify(cache, never()).get(anyString(), any());
        }
    }

    @Nested
    @DisplayName("CachingMouvementStockRepositoryAdapter")
    class MouvementCaching {

        @Mock
        MouvementStockRepositoryAdapter delegate;
        CachingMouvementStockRepositoryAdapter sut;

        @BeforeEach
        void setUp() {
            sut = new CachingMouvementStockRepositoryAdapter(delegate, cache,
                    appProperties(null, null, null, Duration.ofDays(1)));
        }

        @Test
        @DisplayName("save() met en cache le mouvement avec le TTL mouvement")
        void save_metEnCache() {
            MouvementStock mouvement = MouvementStock.creer(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null,
                    EntrepotId.generate(), null, LotId.generate(), MedicamentId.generate(), BigDecimal.TEN, "REF",
                    "Motif", UUID.randomUUID());
            when(delegate.save(mouvement)).thenReturn(mouvement);

            sut.save(mouvement);

            verify(cache).put(anyString(), any(MouvementStockCacheEntry.class), eq(Duration.ofDays(1)));
        }

        @Test
        @DisplayName("cache hit sur findById() → jamais interrogé en base")
        void cacheHit_pasAccesBase() {
            MouvementStock mouvement = MouvementStock.creer(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null,
                    EntrepotId.generate(), null, LotId.generate(), MedicamentId.generate(), BigDecimal.TEN, "REF",
                    "Motif", UUID.randomUUID());
            when(cache.get(anyString(), eq(MouvementStockCacheEntry.class)))
                    .thenReturn(Optional.of(MouvementStockCacheEntry.from(mouvement)));

            sut.findById(mouvement.getId());

            verify(delegate, never()).findById(any());
        }
    }
}
