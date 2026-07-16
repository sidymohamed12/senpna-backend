package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.criteria.EntrepotSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.EntrepotMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.EntrepotJpaRepository;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link EntrepotRepositoryAdapter} et
 * {@link EntrepotQueryAdapter} — vérifient la délégation au repository
 * Spring Data et la traduction domaine ⇆ persistance, sans contexte Spring
 * (contrairement à {@code OrganisationPersistenceAdaptersTest}, taggé
 * {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EntrepotRepositoryAdapter / EntrepotQueryAdapter — unitaire")
class EntrepotPersistenceAdaptersUnitTest {

    @Mock
    EntrepotJpaRepository entrepotJpaRepository;

    @Mock
    EntrepotMapper entrepotMapper;

    EntrepotRepositoryAdapter repositoryAdapter;
    EntrepotQueryAdapter queryAdapter;

    UUID id = UUID.randomUUID();
    UUID regionId = UUID.randomUUID();
    Entrepot entrepot = Entrepot.reconstruct(EntrepotId.of(id), "PNA-CENTRAL", "Pharmacie Nationale",
            TypeEntrepot.PNA_CENTRAL, RegionId.of(regionId), null, null, null, true, java.time.Instant.now(),
            java.time.Instant.now());
    EntrepotJpaEntity entity = new EntrepotJpaEntity(id, "PNA-CENTRAL", "Pharmacie Nationale",
            TypeEntrepot.PNA_CENTRAL, regionId, null, null, null, true);

    @BeforeEach
    void setUp() {
        repositoryAdapter = new EntrepotRepositoryAdapter(entrepotJpaRepository, entrepotMapper);
        queryAdapter = new EntrepotQueryAdapter(entrepotJpaRepository);
    }

    @Nested
    @DisplayName("EntrepotRepositoryAdapter")
    class RepositoryAdapterTests {

        @Test
        @DisplayName("findById() délègue au repository et mappe la présence")
        void findById_present() {
            when(entrepotJpaRepository.findById(id)).thenReturn(Optional.of(entity));
            when(entrepotMapper.toDomain(entity)).thenReturn(entrepot);

            assertThat(repositoryAdapter.findById(EntrepotId.of(id))).contains(entrepot);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(entrepotJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(repositoryAdapter.findById(EntrepotId.of(id))).isEmpty();
        }

        @Test
        @DisplayName("existsByCode() délègue au repository")
        void existsByCode() {
            when(entrepotJpaRepository.existsByCode("PNA-CENTRAL")).thenReturn(true);

            assertThat(repositoryAdapter.existsByCode("PNA-CENTRAL")).isTrue();
        }

        @Test
        @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
        void save() {
            when(entrepotMapper.toEntity(entrepot)).thenReturn(entity);
            when(entrepotJpaRepository.save(entity)).thenReturn(entity);
            when(entrepotMapper.toDomain(entity)).thenReturn(entrepot);

            Entrepot result = repositoryAdapter.save(entrepot);

            assertThat(result).isEqualTo(entrepot);
            verify(entrepotJpaRepository).save(entity);
        }

        @Test
        @DisplayName("search() construit la pagination et mappe le contenu")
        void search() {
            Page<EntrepotJpaEntity> page = new PageImpl<>(List.of(entity));
            when(entrepotJpaRepository.findAll(
                    Mockito.<Specification<EntrepotJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            when(entrepotMapper.toDomain(entity)).thenReturn(entrepot);

            PageResult<Entrepot> result = repositoryAdapter.search(
                    new EntrepotSearchCriteria("pna", TypeEntrepot.PNA_CENTRAL, regionId, true),
                    new PageRequest(0, 20, "code", PageRequest.SortDirection.ASC));

            assertThat(result.content()).containsExactly(entrepot);
        }

        @Test
        @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
        void search_champTriNonAutorise() {
            Page<EntrepotJpaEntity> page = new PageImpl<>(List.of());
            when(entrepotJpaRepository.findAll(
                    Mockito.<Specification<EntrepotJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            PageResult<Entrepot> result = repositoryAdapter.search(EntrepotSearchCriteria.vide(),
                    new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("EntrepotQueryAdapter")
    class QueryAdapterTests {

        @Test
        @DisplayName("findById() mappe l'entité vers une projection")
        void findById_present() {
            when(entrepotJpaRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<EntrepotProjection> result = queryAdapter.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().code()).isEqualTo("PNA-CENTRAL");
            assertThat(result.get().type()).isEqualTo("PNA_CENTRAL");
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(entrepotJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(queryAdapter.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("existsById() délègue au repository")
        void existsById() {
            when(entrepotJpaRepository.existsById(id)).thenReturn(true);

            assertThat(queryAdapter.existsById(id)).isTrue();
        }

        @Test
        @DisplayName("findPnaCentraleActive() mappe la PNA centrale active si présente")
        void findPnaCentraleActive_present() {
            when(entrepotJpaRepository.findFirstByTypeAndActifTrue(TypeEntrepot.PNA_CENTRAL))
                    .thenReturn(Optional.of(entity));

            assertThat(queryAdapter.findPnaCentraleActive()).isPresent();
        }

        @Test
        @DisplayName("findPnaCentraleActive() renvoie vide si aucune PNA centrale active")
        void findPnaCentraleActive_absent() {
            when(entrepotJpaRepository.findFirstByTypeAndActifTrue(TypeEntrepot.PNA_CENTRAL))
                    .thenReturn(Optional.empty());

            assertThat(queryAdapter.findPnaCentraleActive()).isEmpty();
        }

        @Test
        @DisplayName("findPrasActives() mappe chaque PRA active")
        void findPrasActives() {
            when(entrepotJpaRepository.findByTypeAndActifTrueOrderByCodeAsc(TypeEntrepot.PRA))
                    .thenReturn(List.of(entity));

            assertThat(queryAdapter.findPrasActives()).hasSize(1);
        }

        @Test
        @DisplayName("findPrasActivesParRegion() mappe chaque PRA active de la région")
        void findPrasActivesParRegion() {
            when(entrepotJpaRepository.findByTypeAndRegionIdAndActifTrueOrderByCodeAsc(TypeEntrepot.PRA, regionId))
                    .thenReturn(List.of(entity));

            assertThat(queryAdapter.findPrasActivesParRegion(regionId)).hasSize(1);
        }
    }
}
