package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.MouvementSearchCriteria;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.MouvementStockMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.MouvementStockJpaRepository;

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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link MouvementStockRepositoryAdapter} —
 * vérifient la délégation au repository Spring Data et la traduction
 * domaine ⇆ persistance, sans contexte Spring (contrairement à
 * {@code StockPersistenceAdaptersTest}, taggé {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MouvementStockRepositoryAdapter — unitaire")
class MouvementStockRepositoryAdapterUnitTest {

    @Mock
    MouvementStockJpaRepository mouvementStockJpaRepository;

    @Mock
    MouvementStockMapper mouvementStockMapper;

    MouvementStockRepositoryAdapter adapter;

    UUID id = UUID.randomUUID();
    UUID lotId = UUID.randomUUID();
    UUID medicamentId = UUID.randomUUID();
    UUID entrepotDestinationId = UUID.randomUUID();
    UUID utilisateurId = UUID.randomUUID();
    MouvementStock mouvement = MouvementStock.builder()
        .id(MouvementStockId.of(id))
        .typeMouvement(TypeMouvement.ENTREE_ACHAT)
        .sens(SensMouvement.ENTREE)
        .entrepotSourceId(null)
        .entrepotDestinationId(EntrepotId.of(entrepotDestinationId))
        .commandeId(null)
        .lotId(LotId.of(lotId))
        .medicamentId(MedicamentId.of(medicamentId))
        .quantite(BigDecimal.TEN)
        .dateMouvement(Instant.now())
        .referenceDocument("REF-1")
        .motif(null)
        .utilisateurId(utilisateurId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
    MouvementStockJpaEntity entity = MouvementStockJpaEntity.builder()
        .id(id)
        .typeMouvement("ENTREE_ACHAT")
        .sens("ENTREE")
        .entrepotSourceId(null)
        .entrepotDestinationId(entrepotDestinationId)
        .commandeId(null)
        .lotId(lotId)
        .medicamentId(medicamentId)
        .quantite(BigDecimal.TEN)
        .dateMouvement(Instant.now())
        .referenceDocument("REF-1")
        .motif(null)
        .utilisateurId(utilisateurId)
        .build();

    @BeforeEach
    void setUp() {
        adapter = new MouvementStockRepositoryAdapter(mouvementStockJpaRepository, mouvementStockMapper);
    }

    @Test
    @DisplayName("findById() délègue au repository et mappe la présence")
    void findById_present() {
        when(mouvementStockJpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mouvementStockMapper.toDomain(entity)).thenReturn(mouvement);

        assertThat(adapter.findById(MouvementStockId.of(id))).contains(mouvement);
    }

    @Test
    @DisplayName("findById() renvoie vide si absent")
    void findById_absent() {
        when(mouvementStockJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.findById(MouvementStockId.of(id))).isEmpty();
    }

    @Test
    @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
    void save() {
        when(mouvementStockMapper.toEntity(mouvement)).thenReturn(entity);
        when(mouvementStockJpaRepository.save(entity)).thenReturn(entity);
        when(mouvementStockMapper.toDomain(entity)).thenReturn(mouvement);

        MouvementStock result = adapter.save(mouvement);

        assertThat(result).isEqualTo(mouvement);
        verify(mouvementStockJpaRepository).save(entity);
    }

    @Test
    @DisplayName("search() construit la pagination et mappe le contenu")
    void search() {
        Page<MouvementStockJpaEntity> page = new PageImpl<>(List.of(entity));
        when(mouvementStockJpaRepository.findAll(
                Mockito.<Specification<MouvementStockJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);

        when(mouvementStockMapper.toDomain(entity)).thenReturn(mouvement);

        PageResult<MouvementStock> result = adapter.search(
                new MouvementSearchCriteria(lotId, medicamentId, entrepotDestinationId, TypeMouvement.ENTREE_ACHAT,
                        SensMouvement.ENTREE, utilisateurId, Instant.now().minusSeconds(3600), Instant.now()),
                new PageRequest(0, 20, "dateMouvement", PageRequest.SortDirection.DESC));

        assertThat(result.content()).containsExactly(mouvement);
    }

    @Test
    @DisplayName("search() retombe sur dateMouvement si le champ de tri n'est pas autorisé")
    void search_champTriNonAutorise() {
        Page<MouvementStockJpaEntity> page = new PageImpl<>(List.of());
        when(mouvementStockJpaRepository.findAll(
                Mockito.<Specification<MouvementStockJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);

        PageResult<MouvementStock> result = adapter.search(MouvementSearchCriteria.vide(),
                new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

        assertThat(result.content()).isEmpty();
    }
}
