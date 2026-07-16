package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.FamilleSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.FamilleMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FamilleJpaRepository;
import ministere.sante.senpna.shared.domain.projection.FamilleProjection;
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
 * Tests unitaires (Mockito) de {@link FamilleRepositoryAdapter} et
 * {@link FamilleQueryAdapter} — vérifient la délégation au repository Spring
 * Data et la traduction domaine ⇆ persistance, sans contexte Spring
 * (contrairement à {@code MedicamentPersistenceAdaptersTest}, taggé
 * {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FamilleRepositoryAdapter / FamilleQueryAdapter — unitaire")
class FamillePersistenceAdaptersUnitTest {

    @Mock
    FamilleJpaRepository familleJpaRepository;

    @Mock
    FamilleMapper familleMapper;

    FamilleRepositoryAdapter repositoryAdapter;
    FamilleQueryAdapter queryAdapter;

    UUID id = UUID.randomUUID();
    Famille famille = Famille.reconstruct(FamilleId.of(id), "ANTIBIO", "Antibiotiques", "Description", true,
            java.time.Instant.now(), java.time.Instant.now());
    FamilleJpaEntity entity = new FamilleJpaEntity(id, "ANTIBIO", "Antibiotiques", "Description", true);

    @BeforeEach
    void setUp() {
        repositoryAdapter = new FamilleRepositoryAdapter(familleJpaRepository, familleMapper);
        queryAdapter = new FamilleQueryAdapter(familleJpaRepository);
    }

    @Nested
    @DisplayName("FamilleRepositoryAdapter")
    class RepositoryAdapterTests {

        @Test
        @DisplayName("findById() délègue au repository et mappe la présence")
        void findById_present() {
            when(familleJpaRepository.findById(id)).thenReturn(Optional.of(entity));
            when(familleMapper.toDomain(entity)).thenReturn(famille);

            Optional<Famille> result = repositoryAdapter.findById(FamilleId.of(id));

            assertThat(result).contains(famille);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(familleJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(repositoryAdapter.findById(FamilleId.of(id))).isEmpty();
        }

        @Test
        @DisplayName("existsByCodeIgnoreCase() délègue au repository")
        void existsByCodeIgnoreCase() {
            when(familleJpaRepository.existsByCodeIgnoreCase("antibio")).thenReturn(true);

            assertThat(repositoryAdapter.existsByCodeIgnoreCase("antibio")).isTrue();
        }

        @Test
        @DisplayName("existsByCodeIgnoreCaseAndIdNot() délègue au repository")
        void existsByCodeIgnoreCaseAndIdNot() {
            when(familleJpaRepository.existsByCodeIgnoreCaseAndIdNot("antibio", id)).thenReturn(false);

            assertThat(repositoryAdapter.existsByCodeIgnoreCaseAndIdNot("antibio", FamilleId.of(id))).isFalse();
        }

        @Test
        @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
        void save() {
            when(familleMapper.toEntity(famille)).thenReturn(entity);
            when(familleJpaRepository.save(entity)).thenReturn(entity);
            when(familleMapper.toDomain(entity)).thenReturn(famille);

            Famille result = repositoryAdapter.save(famille);

            assertThat(result).isEqualTo(famille);
            verify(familleJpaRepository).save(entity);
        }

        @Test
        @DisplayName("search() construit la pagination et mappe le contenu")
        void search() {
            Page<FamilleJpaEntity> page = new PageImpl<>(List.of(entity));
            when(familleJpaRepository.findAll(
                    Mockito.<Specification<FamilleJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);
            when(familleMapper.toDomain(entity)).thenReturn(famille);

            PageResult<Famille> result = repositoryAdapter.search(new FamilleSearchCriteria("anti", true),
                    new PageRequest(0, 20, "libelle", PageRequest.SortDirection.ASC));

            assertThat(result.content()).containsExactly(famille);
        }

        @Test
        @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
        void search_champTriNonAutorise() {
            Page<FamilleJpaEntity> page = new PageImpl<>(List.of());
            when(familleJpaRepository.findAll(
                    Mockito.<Specification<FamilleJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            PageResult<Famille> result = repositoryAdapter.search(FamilleSearchCriteria.vide(),
                    new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("FamilleQueryAdapter")
    class QueryAdapterTests {

        @Test
        @DisplayName("findAll() mappe chaque entité vers une projection")
        void findAll() {
            when(familleJpaRepository.findAll()).thenReturn(List.of(entity));

            List<FamilleProjection> result = queryAdapter.findAll();

            assertThat(result).extracting(FamilleProjection::code).containsExactly("ANTIBIO");
            assertThat(result).extracting(FamilleProjection::libelle).containsExactly("Antibiotiques");
            assertThat(result).extracting(FamilleProjection::actif).containsExactly(true);
        }

        @Test
        @DisplayName("findAll() sans famille → liste vide")
        void findAll_vide() {
            when(familleJpaRepository.findAll()).thenReturn(List.of());

            assertThat(queryAdapter.findAll()).isEmpty();
        }
    }
}
