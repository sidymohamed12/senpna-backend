package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.FormeSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FormeJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.FormeMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FormeJpaRepository;
import ministere.sante.senpna.shared.domain.projection.FormeProjection;
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
 * Tests unitaires (Mockito) de {@link FormeRepositoryAdapter} et
 * {@link FormeQueryAdapter} — vérifient la délégation au repository Spring
 * Data et la traduction domaine ⇆ persistance, sans contexte Spring
 * (contrairement à {@code MedicamentPersistenceAdaptersTest}, taggé
 * {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FormeRepositoryAdapter / FormeQueryAdapter — unitaire")
class FormePersistenceAdaptersUnitTest {

    @Mock
    FormeJpaRepository formeJpaRepository;

    @Mock
    FormeMapper formeMapper;

    FormeRepositoryAdapter repositoryAdapter;
    FormeQueryAdapter queryAdapter;

    UUID id = UUID.randomUUID();
    Forme forme = Forme.reconstruct(FormeId.of(id), "COMP", "Comprimé", "Forme solide orale", true,
            java.time.Instant.now(), java.time.Instant.now());
    FormeJpaEntity entity = new FormeJpaEntity(id, "COMP", "Comprimé", "Forme solide orale", true);

    @BeforeEach
    void setUp() {
        repositoryAdapter = new FormeRepositoryAdapter(formeJpaRepository, formeMapper);
        queryAdapter = new FormeQueryAdapter(formeJpaRepository);
    }

    @Nested
    @DisplayName("FormeRepositoryAdapter")
    class RepositoryAdapterTests {

        @Test
        @DisplayName("findById() délègue au repository et mappe la présence")
        void findById_present() {
            when(formeJpaRepository.findById(id)).thenReturn(Optional.of(entity));
            when(formeMapper.toDomain(entity)).thenReturn(forme);

            assertThat(repositoryAdapter.findById(FormeId.of(id))).contains(forme);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(formeJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(repositoryAdapter.findById(FormeId.of(id))).isEmpty();
        }

        @Test
        @DisplayName("existsByCodeIgnoreCase() délègue au repository")
        void existsByCodeIgnoreCase() {
            when(formeJpaRepository.existsByCodeIgnoreCase("comp")).thenReturn(true);

            assertThat(repositoryAdapter.existsByCodeIgnoreCase("comp")).isTrue();
        }

        @Test
        @DisplayName("existsByCodeIgnoreCaseAndIdNot() délègue au repository")
        void existsByCodeIgnoreCaseAndIdNot() {
            when(formeJpaRepository.existsByCodeIgnoreCaseAndIdNot("comp", id)).thenReturn(false);

            assertThat(repositoryAdapter.existsByCodeIgnoreCaseAndIdNot("comp", FormeId.of(id))).isFalse();
        }

        @Test
        @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
        void save() {
            when(formeMapper.toEntity(forme)).thenReturn(entity);
            when(formeJpaRepository.save(entity)).thenReturn(entity);
            when(formeMapper.toDomain(entity)).thenReturn(forme);

            Forme result = repositoryAdapter.save(forme);

            assertThat(result).isEqualTo(forme);
            verify(formeJpaRepository).save(entity);
        }

        @Test
        @DisplayName("search() construit la pagination et mappe le contenu")
        void search() {
            Page<FormeJpaEntity> page = new PageImpl<>(List.of(entity));
            when(formeJpaRepository.findAll(
                    Mockito.<Specification<FormeJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            when(formeMapper.toDomain(entity)).thenReturn(forme);

            PageResult<Forme> result = repositoryAdapter.search(new FormeSearchCriteria("comp", true),
                    new PageRequest(0, 20, "libelle", PageRequest.SortDirection.ASC));

            assertThat(result.content()).containsExactly(forme);
        }

        @Test
        @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
        void search_champTriNonAutorise() {
            Page<FormeJpaEntity> page = new PageImpl<>(List.of());
            when(formeJpaRepository.findAll(
                    Mockito.<Specification<FormeJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            PageResult<Forme> result = repositoryAdapter.search(FormeSearchCriteria.vide(),
                    new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("FormeQueryAdapter")
    class QueryAdapterTests {

        @Test
        @DisplayName("findAll() mappe chaque entité vers une projection")
        void findAll() {
            when(formeJpaRepository.findAll()).thenReturn(List.of(entity));

            List<FormeProjection> result = queryAdapter.findAll();

            assertThat(result).extracting(FormeProjection::code).containsExactly("COMP");
            assertThat(result).extracting(FormeProjection::libelle).containsExactly("Comprimé");
            assertThat(result).extracting(FormeProjection::actif).containsExactly(true);
        }

        @Test
        @DisplayName("findAll() sans forme → liste vide")
        void findAll_vide() {
            when(formeJpaRepository.findAll()).thenReturn(List.of());

            assertThat(queryAdapter.findAll()).isEmpty();
        }
    }
}
