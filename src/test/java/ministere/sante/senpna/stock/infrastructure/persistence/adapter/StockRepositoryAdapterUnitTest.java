package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.StockMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.StockJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link StockRepositoryAdapter} — vérifient la
 * délégation au repository Spring Data et la traduction domaine ⇆
 * persistance, sans contexte Spring (contrairement à
 * {@code StockPersistenceAdaptersTest}, taggé {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockRepositoryAdapter — unitaire")
class StockRepositoryAdapterUnitTest {

    @Mock
    StockJpaRepository stockJpaRepository;

    @Mock
    StockMapper stockMapper;

    StockRepositoryAdapter adapter;

    UUID id = UUID.randomUUID();
    UUID entrepotId = UUID.randomUUID();
    UUID lotId = UUID.randomUUID();
    UUID medicamentId = UUID.randomUUID();
    Stock stock = Stock.builder()
        .id(StockId.of(id))
        .entrepotId(EntrepotId.of(entrepotId))
        .lotId(LotId.of(lotId))
        .medicamentId(MedicamentId.of(medicamentId))
        .quantiteDisponible(new BigDecimal("100.0000"))
        .quantiteReservee(BigDecimal.ZERO)
        .quantiteEnCommande(BigDecimal.ZERO)
        .seuilAlerte(new BigDecimal("10.0000"))
        .createdAt(java.time.Instant.now())
        .updatedAt(java.time.Instant.now())
        .build();
    StockJpaEntity entity = StockJpaEntity.builder()
        .id(id)
        .entrepotId(entrepotId)
        .lotId(lotId)
        .medicamentId(medicamentId)
        .quantiteDisponible(new BigDecimal("100.0000"))
        .quantiteReservee(BigDecimal.ZERO)
        .quantiteEnCommande(BigDecimal.ZERO)
        .seuilAlerte(new BigDecimal("10.0000"))
        .build();

    @BeforeEach
    void setUp() {
        adapter = new StockRepositoryAdapter(stockJpaRepository, stockMapper);
    }

    @Test
    @DisplayName("findById() délègue au repository et mappe la présence")
    void findById_present() {
        when(stockJpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(stockMapper.toDomain(entity)).thenReturn(stock);

        assertThat(adapter.findById(StockId.of(id))).contains(stock);
    }

    @Test
    @DisplayName("findById() renvoie vide si absent")
    void findById_absent() {
        when(stockJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.findById(StockId.of(id))).isEmpty();
    }

    @Test
    @DisplayName("findByEntrepotIdAndLotId() délègue au repository et mappe la présence")
    void findByEntrepotIdAndLotId_present() {
        when(stockJpaRepository.findByEntrepotIdAndLotId(entrepotId, lotId)).thenReturn(Optional.of(entity));
        when(stockMapper.toDomain(entity)).thenReturn(stock);

        assertThat(adapter.findByEntrepotIdAndLotId(EntrepotId.of(entrepotId), LotId.of(lotId))).contains(stock);
    }

    @Test
    @DisplayName("findByEntrepotIdAndLotId() renvoie vide si absent")
    void findByEntrepotIdAndLotId_absent() {
        when(stockJpaRepository.findByEntrepotIdAndLotId(entrepotId, lotId)).thenReturn(Optional.empty());

        assertThat(adapter.findByEntrepotIdAndLotId(EntrepotId.of(entrepotId), LotId.of(lotId))).isEmpty();
    }

    @Test
    @DisplayName("findByEntrepotIdAndLotIdForUpdate() délègue au repository (verrou pessimiste) et mappe la présence")
    void findByEntrepotIdAndLotIdForUpdate_present() {
        when(stockJpaRepository.findByEntrepotIdAndLotIdForUpdate(entrepotId, lotId))
                .thenReturn(Optional.of(entity));
        when(stockMapper.toDomain(entity)).thenReturn(stock);

        assertThat(adapter.findByEntrepotIdAndLotIdForUpdate(EntrepotId.of(entrepotId), LotId.of(lotId)))
                .contains(stock);
    }

    @Test
    @DisplayName("findByEntrepotIdAndLotIdForUpdate() renvoie vide si absent")
    void findByEntrepotIdAndLotIdForUpdate_absent() {
        when(stockJpaRepository.findByEntrepotIdAndLotIdForUpdate(entrepotId, lotId)).thenReturn(Optional.empty());

        assertThat(adapter.findByEntrepotIdAndLotIdForUpdate(EntrepotId.of(entrepotId), LotId.of(lotId))).isEmpty();
    }

    @Test
    @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
    void save() {
        when(stockMapper.toEntity(stock)).thenReturn(entity);
        when(stockJpaRepository.save(entity)).thenReturn(entity);
        when(stockMapper.toDomain(entity)).thenReturn(stock);

        Stock result = adapter.save(stock);

        assertThat(result).isEqualTo(stock);
        verify(stockJpaRepository).save(entity);
    }

    @Test
    @DisplayName("search() construit la pagination et mappe le contenu")
    void search() {
        Page<StockJpaEntity> page = new PageImpl<>(List.of(entity));
        when(stockJpaRepository.findAll(
                Mockito.<Specification<StockJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);

        when(stockMapper.toDomain(entity)).thenReturn(stock);

        PageResult<Stock> result = adapter.search(
                new StockSearchCriteria(entrepotId, lotId, medicamentId, true, true),
                new PageRequest(0, 20, "quantiteDisponible", PageRequest.SortDirection.ASC));

        assertThat(result.content()).containsExactly(stock);
    }

    @Test
    @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
    void search_champTriNonAutorise() {
        Page<StockJpaEntity> page = new PageImpl<>(List.of());
        when(stockJpaRepository.findAll(
                Mockito.<Specification<StockJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);

        PageResult<Stock> result = adapter.search(StockSearchCriteria.vide(),
                new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

        assertThat(result.content()).isEmpty();
    }
}
