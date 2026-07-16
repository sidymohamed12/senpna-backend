package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.MedicamentSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.MedicamentMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FamilleJpaRepository;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.MedicamentJpaRepository;
import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link MedicamentRepositoryAdapter} et
 * {@link MedicamentQueryAdapter} — vérifient la délégation au repository
 * Spring Data et la traduction domaine ⇆ persistance, sans contexte Spring
 * (contrairement à {@code MedicamentPersistenceAdaptersTest}, taggé
 * {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MedicamentRepositoryAdapter / MedicamentQueryAdapter — unitaire")
class MedicamentPersistenceAdaptersUnitTest {

    @Mock
    MedicamentJpaRepository medicamentJpaRepository;

    @Mock
    FamilleJpaRepository familleJpaRepository;

    @Mock
    MedicamentMapper medicamentMapper;

    MedicamentRepositoryAdapter repositoryAdapter;
    MedicamentQueryAdapter queryAdapter;

    UUID id = UUID.randomUUID();
    UUID familleId = UUID.randomUUID();
    UUID formeId = UUID.randomUUID();
    Medicament medicament = Medicament.reconstruct(MedicamentId.of(id), "MED-1", "Doliprane", "Paracétamol", "500mg",
            FormeId.of(formeId), FamilleId.of(familleId), null, null, null, null, false, "Sanofi", null, null, true,
            java.time.Instant.now(), java.time.Instant.now());
    MedicamentJpaEntity entity = new MedicamentJpaEntity(id, "MED-1", "Doliprane", "Paracétamol", "500mg", formeId,
            familleId, null, null, null, null, false, "Sanofi", null, null, true);

    @BeforeEach
    void setUp() {
        repositoryAdapter = new MedicamentRepositoryAdapter(medicamentJpaRepository, medicamentMapper);
        queryAdapter = new MedicamentQueryAdapter(medicamentJpaRepository, familleJpaRepository);
    }

    @Nested
    @DisplayName("MedicamentRepositoryAdapter")
    class RepositoryAdapterTests {

        @Test
        @DisplayName("findById() délègue au repository et mappe la présence")
        void findById_present() {
            when(medicamentJpaRepository.findById(id)).thenReturn(Optional.of(entity));
            when(medicamentMapper.toDomain(entity)).thenReturn(medicament);

            assertThat(repositoryAdapter.findById(MedicamentId.of(id))).contains(medicament);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
            when(medicamentJpaRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(repositoryAdapter.findById(MedicamentId.of(id))).isEmpty();
        }

        @Test
        @DisplayName("existsByCodeIgnoreCase() délègue au repository")
        void existsByCodeIgnoreCase() {
            when(medicamentJpaRepository.existsByCodeIgnoreCase("med-1")).thenReturn(true);

            assertThat(repositoryAdapter.existsByCodeIgnoreCase("med-1")).isTrue();
        }

        @Test
        @DisplayName("existsByCodeIgnoreCaseAndIdNot() délègue au repository")
        void existsByCodeIgnoreCaseAndIdNot() {
            when(medicamentJpaRepository.existsByCodeIgnoreCaseAndIdNot("med-1", id)).thenReturn(false);

            assertThat(repositoryAdapter.existsByCodeIgnoreCaseAndIdNot("med-1", MedicamentId.of(id))).isFalse();
        }

        @Test
        @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
        void save() {
            when(medicamentMapper.toEntity(medicament)).thenReturn(entity);
            when(medicamentJpaRepository.save(entity)).thenReturn(entity);
            when(medicamentMapper.toDomain(entity)).thenReturn(medicament);

            Medicament result = repositoryAdapter.save(medicament);

            assertThat(result).isEqualTo(medicament);
            verify(medicamentJpaRepository).save(entity);
        }

        @Test
        @DisplayName("search() construit la pagination et mappe le contenu")
        void search() {
            Page<MedicamentJpaEntity> page = new PageImpl<>(List.of(entity));
            when(medicamentJpaRepository.findAll(
                    Mockito.<Specification<MedicamentJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            when(medicamentMapper.toDomain(entity)).thenReturn(medicament);

            PageResult<Medicament> result = repositoryAdapter.search(
                    new MedicamentSearchCriteria("doli", familleId, formeId, true),
                    new PageRequest(0, 20, "code", PageRequest.SortDirection.ASC));

            assertThat(result.content()).containsExactly(medicament);
        }

        @Test
        @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
        void search_champTriNonAutorise() {
            Page<MedicamentJpaEntity> page = new PageImpl<>(List.of());
            when(medicamentJpaRepository.findAll(
                    Mockito.<Specification<MedicamentJpaEntity>>any(),
                    any(Pageable.class)))
                    .thenReturn(page);

            PageResult<Medicament> result = repositoryAdapter.search(MedicamentSearchCriteria.vide(),
                    new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("MedicamentQueryAdapter")
    class QueryAdapterTests {

        @Test
        @DisplayName("findAllById() enrichit chaque médicament du libellé de sa famille")
        void findAllById() {
            FamilleJpaEntity familleEntity = new FamilleJpaEntity(familleId, "ANTALG", "Antalgiques", null, true);
            when(medicamentJpaRepository.findAllById(Set.of(id))).thenReturn(List.of(entity));
            when(familleJpaRepository.findAllById(List.of(familleId))).thenReturn(List.of(familleEntity));

            List<MedicamentProjection> result = queryAdapter.findAllById(Set.of(id));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).code()).isEqualTo("MED-1");
            assertThat(result.get(0).familleNom()).isEqualTo("Antalgiques");
            assertThat(result.get(0).fabricant()).isEqualTo("Sanofi");
            assertThat(result.get(0).actif()).isTrue();
        }

        @Test
        @DisplayName("findAllById() avec identifiants inconnus → liste vide")
        void findAllById_vide() {
            when(medicamentJpaRepository.findAllById(Set.of(id))).thenReturn(List.of());
            when(familleJpaRepository.findAllById(List.of())).thenReturn(List.of());

            assertThat(queryAdapter.findAllById(Set.of(id))).isEmpty();
        }
    }
}
