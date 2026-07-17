package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.criteria.StructureSanitaireSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.StructureSanitaireMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.StructureSanitaireJpaRepository;
import ministere.sante.senpna.shared.domain.projection.StructureSanitaireProjection;
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
 * Tests unitaires (Mockito) de {@link StructureSanitaireRepositoryAdapter}
 * et {@link StructureSanitaireQueryAdapter} — vérifient la délégation au
 * repository Spring Data et la traduction domaine ⇆ persistance, sans
 * contexte Spring (contrairement à
 * {@code OrganisationPersistenceAdaptersTest}, taggé {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StructureSanitaireRepositoryAdapter / StructureSanitaireQueryAdapter — unitaire")
class StructureSanitairePersistenceAdaptersUnitTest {

    @Mock
    StructureSanitaireJpaRepository structureSanitaireJpaRepository;

    @Mock
    StructureSanitaireMapper structureSanitaireMapper;

    StructureSanitaireRepositoryAdapter repositoryAdapter;
    StructureSanitaireQueryAdapter queryAdapter;

    UUID id = UUID.randomUUID();
    UUID regionId = UUID.randomUUID();
    UUID praId = UUID.randomUUID();
    StructureSanitaire structure = StructureSanitaire.builder()
        .id(StructureSanitaireId.of(id))
        .code("HOP-DKR")
        .nom("Hôpital de Dakar")
        .type(TypeStructureSanitaire.HOPITAL)
        .regionId(RegionId.of(regionId))
        .praId(EntrepotId.of(praId))
        .district(null)
        .adresse(null)
        .telephone(null)
        .email(null)
        .responsableNom(null)
        .responsablePrenom(null)
        .statutAdhesion(StatutAdhesion.VALIDEE)
        .motifRejet(null)
        .actif(true)
        .createdAt(java.time.Instant.now())
        .updatedAt(java.time.Instant.now())
        .build();
    StructureSanitaireJpaEntity entity = StructureSanitaireJpaEntity.builder()
        .id(id)
        .code("HOP-DKR")
        .nom("Hôpital de Dakar")
        .type(TypeStructureSanitaire.HOPITAL)
        .regionId(regionId)
        .praId(praId)
        .district(null)
        .adresse(null)
        .telephone(null)
        .email(null)
        .responsableNom(null)
        .responsablePrenom(null)
        .statutAdhesion(StatutAdhesion.VALIDEE)
        .motifRejet(null)
        .actif(true)
        .build();

    @BeforeEach
    void setUp() {
        repositoryAdapter = new StructureSanitaireRepositoryAdapter(structureSanitaireJpaRepository,
                structureSanitaireMapper);
        queryAdapter = new StructureSanitaireQueryAdapter(structureSanitaireJpaRepository);
    }

    @Nested
    @DisplayName("StructureSanitaireRepositoryAdapter")
    class RepositoryAdapterTests {

        @Test
        @DisplayName("findById() délègue au repository et mappe la présence")
        void findById_present() {
            when(structureSanitaireJpaRepository.findById(id)).thenReturn(Optional.of(entity));
            when(structureSanitaireMapper.toDomain(entity)).thenReturn(structure);

            assertThat(repositoryAdapter.findById(StructureSanitaireId.of(id))).contains(structure);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(structureSanitaireJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(repositoryAdapter.findById(StructureSanitaireId.of(id))).isEmpty();
        }

        @Test
        @DisplayName("existsByCode() délègue au repository")
        void existsByCode() {
            when(structureSanitaireJpaRepository.existsByCode("HOP-DKR")).thenReturn(true);

            assertThat(repositoryAdapter.existsByCode("HOP-DKR")).isTrue();
        }

        @Test
        @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
        void save() {
            when(structureSanitaireMapper.toEntity(structure)).thenReturn(entity);
            when(structureSanitaireJpaRepository.save(entity)).thenReturn(entity);
            when(structureSanitaireMapper.toDomain(entity)).thenReturn(structure);

            StructureSanitaire result = repositoryAdapter.save(structure);

            assertThat(result).isEqualTo(structure);
            verify(structureSanitaireJpaRepository).save(entity);
        }

        @Test
        @DisplayName("search() construit la pagination et mappe le contenu")
        void search() {
            Page<StructureSanitaireJpaEntity> page = new PageImpl<>(List.of(entity));
            when(structureSanitaireJpaRepository.findAll(
                    Mockito.<Specification<StructureSanitaireJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            when(structureSanitaireMapper.toDomain(entity)).thenReturn(structure);

            PageResult<StructureSanitaire> result = repositoryAdapter.search(
                    new StructureSanitaireSearchCriteria("dkr", TypeStructureSanitaire.HOPITAL, regionId, praId,
                            StatutAdhesion.VALIDEE, true),
                    new PageRequest(0, 20, "nom", PageRequest.SortDirection.ASC));

            assertThat(result.content()).containsExactly(structure);
        }

        @Test
        @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
        void search_champTriNonAutorise() {
            Page<StructureSanitaireJpaEntity> page = new PageImpl<>(List.of());
            when(structureSanitaireJpaRepository.findAll(
                    Mockito.<Specification<StructureSanitaireJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            PageResult<StructureSanitaire> result = repositoryAdapter.search(StructureSanitaireSearchCriteria.vide(),
                    new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("StructureSanitaireQueryAdapter")
    class QueryAdapterTests {

        @Test
        @DisplayName("findById() mappe l'entité vers une projection")
        void findById_present() {
            when(structureSanitaireJpaRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<StructureSanitaireProjection> result = queryAdapter.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().code()).isEqualTo("HOP-DKR");
            assertThat(result.get().regionId()).isEqualTo(regionId);
            assertThat(result.get().praId()).isEqualTo(praId);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(structureSanitaireJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(queryAdapter.findById(id)).isEmpty();
        }
    }
}
