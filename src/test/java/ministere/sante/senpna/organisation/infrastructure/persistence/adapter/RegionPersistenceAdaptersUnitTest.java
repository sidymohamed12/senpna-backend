package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.RegionJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.RegionMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.RegionJpaRepository;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link RegionRepositoryAdapter} et
 * {@link RegionQueryAdapter} — vérifient la délégation au repository
 * Spring Data et la traduction domaine ⇆ persistance, sans contexte Spring
 * (contrairement à {@code OrganisationPersistenceAdaptersTest}, taggé
 * {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegionRepositoryAdapter / RegionQueryAdapter — unitaire")
class RegionPersistenceAdaptersUnitTest {

    @Mock
    RegionJpaRepository regionJpaRepository;

    @Mock
    RegionMapper regionMapper;

    RegionRepositoryAdapter repositoryAdapter;
    RegionQueryAdapter queryAdapter;

    UUID id = UUID.randomUUID();
    Region region = Region.reconstruct(RegionId.of(id), "DK", "Dakar", true, java.time.Instant.now(),
            java.time.Instant.now());
    RegionJpaEntity entity = new RegionJpaEntity(id, "DK", "Dakar", true);

    @BeforeEach
    void setUp() {
        repositoryAdapter = new RegionRepositoryAdapter(regionJpaRepository, regionMapper);
        queryAdapter = new RegionQueryAdapter(regionJpaRepository);
    }

    @Nested
    @DisplayName("RegionRepositoryAdapter")
    class RepositoryAdapterTests {

        @Test
        @DisplayName("findById() délègue au repository et mappe la présence")
        void findById_present() {
            when(regionJpaRepository.findById(id)).thenReturn(Optional.of(entity));
            when(regionMapper.toDomain(entity)).thenReturn(region);

            assertThat(repositoryAdapter.findById(RegionId.of(id))).contains(region);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(regionJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(repositoryAdapter.findById(RegionId.of(id))).isEmpty();
        }

        @Test
        @DisplayName("existsByCode() délègue au repository")
        void existsByCode() {
            when(regionJpaRepository.existsByCode("DK")).thenReturn(true);

            assertThat(repositoryAdapter.existsByCode("DK")).isTrue();
        }

        @Test
        @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
        void save() {
            when(regionMapper.toEntity(region)).thenReturn(entity);
            when(regionJpaRepository.save(entity)).thenReturn(entity);
            when(regionMapper.toDomain(entity)).thenReturn(region);

            Region result = repositoryAdapter.save(region);

            assertThat(result).isEqualTo(region);
            verify(regionJpaRepository).save(entity);
        }

        @Test
        @DisplayName("findAll() trie par nom ascendant et mappe le contenu")
        void findAll() {
            when(regionJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "nom"))).thenReturn(List.of(entity));
            when(regionMapper.toDomain(entity)).thenReturn(region);

            assertThat(repositoryAdapter.findAll()).containsExactly(region);
        }
    }

    @Nested
    @DisplayName("RegionQueryAdapter")
    class QueryAdapterTests {

        @Test
        @DisplayName("findAll() mappe chaque région vers une projection")
        void findAll() {
            when(regionJpaRepository.findAll()).thenReturn(List.of(entity));

            List<RegionProjection> result = queryAdapter.findAll();

            assertThat(result).extracting(RegionProjection::code).containsExactly("DK");
        }

        @Test
        @DisplayName("findByCode() mappe la région si présente")
        void findByCode_present() {
            when(regionJpaRepository.findByCode("DK")).thenReturn(Optional.of(entity));

            assertThat(queryAdapter.findByCode("DK")).isPresent();
        }

        @Test
        @DisplayName("findByCode() renvoie vide si absent")
        void findByCode_absent() {
            when(regionJpaRepository.findByCode("DK")).thenReturn(Optional.empty());

            assertThat(queryAdapter.findByCode("DK")).isEmpty();
        }

        @Test
        @DisplayName("findById() mappe la région si présente")
        void findById_present() {
            when(regionJpaRepository.findById(id)).thenReturn(Optional.of(entity));

            assertThat(queryAdapter.findById(id)).isPresent();
        }

        @Test
        @DisplayName("findById() renvoie vide si absente")
        void findById_absent() {
            when(regionJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(queryAdapter.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("existsById() délègue au repository")
        void existsById() {
            when(regionJpaRepository.existsById(id)).thenReturn(true);

            assertThat(queryAdapter.existsById(id)).isTrue();
        }
    }
}
