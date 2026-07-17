package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.ConditionnementSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.ConditionnementMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.ConditionnementJpaRepository;
import ministere.sante.senpna.shared.domain.projection.ConditionnementProjection;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link ConditionnementRepositoryAdapter} et
 * {@link ConditionnementQueryAdapter} — vérifient la délégation au
 * repository Spring Data et la traduction domaine ⇆ persistance, sans
 * contexte Spring (contrairement à {@code MedicamentPersistenceAdaptersTest},
 * taggé {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConditionnementRepositoryAdapter / ConditionnementQueryAdapter — unitaire")
class ConditionnementPersistenceAdaptersUnitTest {

        @Mock
        ConditionnementJpaRepository conditionnementJpaRepository;

        @Mock
        ConditionnementMapper conditionnementMapper;

        ConditionnementRepositoryAdapter repositoryAdapter;
        ConditionnementQueryAdapter queryAdapter;

        UUID id = UUID.randomUUID();
        UUID medicamentId = UUID.randomUUID();
        Conditionnement conditionnement = Conditionnement.builder()
            .id(ConditionnementId.of(id))
            .medicamentId(MedicamentId.of(medicamentId))
            .nom("Boîte de 20")
            .niveau(1)
            .quantiteUniteBase(BigDecimal.TEN)
            .estUniteBase(false)
            .prixAchat(BigDecimal.valueOf(500))
            .prixVente(BigDecimal.valueOf(750))
            .actif(true)
            .createdAt(java.time.Instant.now())
            .updatedAt(java.time.Instant.now())
            .build();
        ConditionnementJpaEntity entity = ConditionnementJpaEntity.builder()
            .id(id)
            .medicamentId(medicamentId)
            .nom("Boîte de 20")
            .niveau(1)
            .quantiteUniteBase(BigDecimal.TEN)
            .estUniteBase(false)
            .prixAchat(BigDecimal.valueOf(500))
            .prixVente(BigDecimal.valueOf(750))
            .actif(true)
            .build();

        @BeforeEach
        void setUp() {
                repositoryAdapter = new ConditionnementRepositoryAdapter(conditionnementJpaRepository,
                                conditionnementMapper);
                queryAdapter = new ConditionnementQueryAdapter(conditionnementJpaRepository);
        }

        @Nested
        @DisplayName("ConditionnementRepositoryAdapter")
        class RepositoryAdapterTests {

                @Test
                @DisplayName("findById() délègue au repository et mappe la présence")
                void findById_present() {
                        when(conditionnementJpaRepository.findById(id)).thenReturn(Optional.of(entity));
                        when(conditionnementMapper.toDomain(entity)).thenReturn(conditionnement);

                        assertThat(repositoryAdapter.findById(ConditionnementId.of(id))).contains(conditionnement);
                }

                @Test
                @DisplayName("findById() renvoie vide si absent")
                void findById_absent() {
                        when(conditionnementJpaRepository.findById(id)).thenReturn(Optional.empty());

                        assertThat(repositoryAdapter.findById(ConditionnementId.of(id))).isEmpty();
                }

                @Test
                @DisplayName("existsByMedicamentIdAndNiveau() délègue au repository")
                void existsByMedicamentIdAndNiveau() {
                        when(conditionnementJpaRepository.existsByMedicamentIdAndNiveau(medicamentId, 1))
                                        .thenReturn(true);

                        assertThat(repositoryAdapter.existsByMedicamentIdAndNiveau(MedicamentId.of(medicamentId), 1))
                                        .isTrue();
                }

                @Test
                @DisplayName("existsByMedicamentIdAndNiveauAndIdNot() délègue au repository")
                void existsByMedicamentIdAndNiveauAndIdNot() {
                        when(conditionnementJpaRepository.existsByMedicamentIdAndNiveauAndIdNot(medicamentId, 1, id))
                                        .thenReturn(false);

                        assertThat(repositoryAdapter.existsByMedicamentIdAndNiveauAndIdNot(
                                        MedicamentId.of(medicamentId), 1,
                                        ConditionnementId.of(id))).isFalse();
                }

                @Test
                @DisplayName("existsByMedicamentIdAndNomIgnoreCase() délègue au repository")
                void existsByMedicamentIdAndNomIgnoreCase() {
                        when(conditionnementJpaRepository.existsByMedicamentIdAndNomIgnoreCase(medicamentId,
                                        "boîte de 20"))
                                        .thenReturn(true);

                        assertThat(repositoryAdapter.existsByMedicamentIdAndNomIgnoreCase(MedicamentId.of(medicamentId),
                                        "boîte de 20")).isTrue();
                }

                @Test
                @DisplayName("existsByMedicamentIdAndNomIgnoreCaseAndIdNot() délègue au repository")
                void existsByMedicamentIdAndNomIgnoreCaseAndIdNot() {
                        when(conditionnementJpaRepository.existsByMedicamentIdAndNomIgnoreCaseAndIdNot(medicamentId,
                                        "boîte de 20", id)).thenReturn(false);

                        assertThat(repositoryAdapter.existsByMedicamentIdAndNomIgnoreCaseAndIdNot(
                                        MedicamentId.of(medicamentId),
                                        "boîte de 20", ConditionnementId.of(id))).isFalse();
                }

                @Test
                @DisplayName("existsUniteBaseByMedicamentId() délègue au repository")
                void existsUniteBaseByMedicamentId() {
                        when(conditionnementJpaRepository.existsByMedicamentIdAndEstUniteBaseTrue(medicamentId))
                                        .thenReturn(true);

                        assertThat(repositoryAdapter.existsUniteBaseByMedicamentId(MedicamentId.of(medicamentId)))
                                        .isTrue();
                }

                @Test
                @DisplayName("existsUniteBaseByMedicamentIdAndIdNot() délègue au repository")
                void existsUniteBaseByMedicamentIdAndIdNot() {
                        when(conditionnementJpaRepository.existsByMedicamentIdAndEstUniteBaseTrueAndIdNot(medicamentId,
                                        id))
                                        .thenReturn(false);

                        assertThat(repositoryAdapter.existsUniteBaseByMedicamentIdAndIdNot(
                                        MedicamentId.of(medicamentId),
                                        ConditionnementId.of(id))).isFalse();
                }

                @Test
                @DisplayName("estUniqueUniteBaseActive() vrai quand le compte est ≤ 1")
                void estUniqueUniteBaseActive_vrai() {
                        when(conditionnementJpaRepository
                                        .countByMedicamentIdAndEstUniteBaseTrueAndActifTrue(medicamentId))
                                        .thenReturn(1L);

                        assertThat(repositoryAdapter.estUniqueUniteBaseActive(MedicamentId.of(medicamentId),
                                        ConditionnementId.of(id))).isTrue();
                }

                @Test
                @DisplayName("estUniqueUniteBaseActive() faux quand le compte est > 1")
                void estUniqueUniteBaseActive_faux() {
                        when(conditionnementJpaRepository
                                        .countByMedicamentIdAndEstUniteBaseTrueAndActifTrue(medicamentId))
                                        .thenReturn(2L);

                        assertThat(repositoryAdapter.estUniqueUniteBaseActive(MedicamentId.of(medicamentId),
                                        ConditionnementId.of(id))).isFalse();
                }

                @Test
                @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
                void save() {
                        when(conditionnementMapper.toEntity(conditionnement)).thenReturn(entity);
                        when(conditionnementJpaRepository.save(entity)).thenReturn(entity);
                        when(conditionnementMapper.toDomain(entity)).thenReturn(conditionnement);

                        Conditionnement result = repositoryAdapter.save(conditionnement);

                        assertThat(result).isEqualTo(conditionnement);
                        verify(conditionnementJpaRepository).save(entity);
                }

                @Test
                @DisplayName("search() construit la pagination et mappe le contenu")
                void search() {
                        Page<ConditionnementJpaEntity> page = new PageImpl<>(List.of(entity));
                        when(conditionnementJpaRepository.findAll(
                                        Mockito.<Specification<ConditionnementJpaEntity>>any(),
                                        any(Pageable.class)))
                                        .thenReturn(page);
                        when(conditionnementMapper.toDomain(entity)).thenReturn(conditionnement);

                        PageResult<Conditionnement> result = repositoryAdapter.search(
                                        new ConditionnementSearchCriteria(medicamentId, true),
                                        new PageRequest(0, 20, "nom", PageRequest.SortDirection.ASC));

                        assertThat(result.content()).containsExactly(conditionnement);
                }

                @Test
                @DisplayName("search() retombe sur niveau si le champ de tri n'est pas autorisé")
                void search_champTriNonAutorise() {
                        Page<ConditionnementJpaEntity> page = new PageImpl<>(List.of());
                        when(conditionnementJpaRepository.findAll(
                                        Mockito.<Specification<ConditionnementJpaEntity>>any(),
                                        any(Pageable.class)))
                                        .thenReturn(page);

                        PageResult<Conditionnement> result = repositoryAdapter.search(
                                        ConditionnementSearchCriteria.vide(),
                                        new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

                        assertThat(result.content()).isEmpty();
                }
        }

        @Nested
        @DisplayName("ConditionnementQueryAdapter")
        class QueryAdapterTests {

                @Test
                @DisplayName("findAllVendablesByMedicamentIdIn() mappe chaque conditionnement vendable")
                void findAllVendables() {
                        when(conditionnementJpaRepository
                                        .findByMedicamentIdInAndActifTrueAndPrixVenteIsNotNullOrderByMedicamentIdAscNiveauAsc(
                                                        Set.of(medicamentId)))
                                        .thenReturn(List.of(entity));

                        List<ConditionnementProjection> result = queryAdapter.findAllVendablesByMedicamentIdIn(
                                        Set.of(medicamentId));

                        assertThat(result).hasSize(1);
                        assertThat(result.get(0).nom()).isEqualTo("Boîte de 20");
                        assertThat(result.get(0).medicamentId()).isEqualTo(medicamentId);
                }

                @Test
                @DisplayName("findAllVendablesByMedicamentIdIn() avec collection null → liste vide, sans appel repository")
                void findAllVendables_collectionNulle() {
                        assertThat(queryAdapter.findAllVendablesByMedicamentIdIn(null)).isEmpty();
                }

                @Test
                @DisplayName("findAllVendablesByMedicamentIdIn() avec collection vide → liste vide, sans appel repository")
                void findAllVendables_collectionVide() {
                        assertThat(queryAdapter.findAllVendablesByMedicamentIdIn(Set.of())).isEmpty();
                }
        }
}
