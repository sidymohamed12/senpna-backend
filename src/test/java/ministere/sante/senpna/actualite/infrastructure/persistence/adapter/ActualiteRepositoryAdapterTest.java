package ministere.sante.senpna.actualite.infrastructure.persistence.adapter;

import ministere.sante.senpna.actualite.domain.criteria.ActualiteSearchCriteria;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteJpaEntity;
import ministere.sante.senpna.actualite.infrastructure.persistence.mapper.ActualiteMapper;
import ministere.sante.senpna.actualite.infrastructure.persistence.repository.ActualiteJpaRepository;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActualiteRepositoryAdapter — persistance des actualités")
class ActualiteRepositoryAdapterTest {

    @Mock
    private ActualiteJpaRepository actualiteJpaRepository;
    @Mock
    private ActualiteMapper actualiteMapper;

    private ActualiteRepositoryAdapter adapter;

    private static final UUID ACTUALITE_ID = UUID.randomUUID();
    private static final UUID AUTEUR_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new ActualiteRepositoryAdapter(actualiteJpaRepository, actualiteMapper);
    }

    @Test
    @DisplayName("findById() retourne l'actualité mappée quand l'entité existe")
    void findById_existe() {
        ActualiteJpaEntity entity = new ActualiteJpaEntity(ACTUALITE_ID, CategorieActualite.PROJET, "Titre", null,
                AUTEUR_ID, "Auteur", List.of(), StatutActualite.BROUILLON);
        Actualite actualite = actualiteExistante();
        when(actualiteJpaRepository.findById(ACTUALITE_ID)).thenReturn(Optional.of(entity));
        when(actualiteMapper.toDomain(entity)).thenReturn(actualite);

        Optional<Actualite> result = adapter.findById(ActualiteId.of(ACTUALITE_ID));

        assertThat(result).contains(actualite);
    }

    @Test
    @DisplayName("findById() retourne vide quand l'entité n'existe pas")
    void findById_inexistante() {
        when(actualiteJpaRepository.findById(ACTUALITE_ID)).thenReturn(Optional.empty());

        Optional<Actualite> result = adapter.findById(ActualiteId.of(ACTUALITE_ID));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save() met à jour l'entité managée existante quand elle est déjà présente en base")
    void save_entiteExistante_metAJour() {
        Actualite actualite = actualiteExistante();
        ActualiteJpaEntity entiteExistante = new ActualiteJpaEntity(ACTUALITE_ID, CategorieActualite.PROJET,
                "Ancien titre", null, AUTEUR_ID, "Auteur", List.of(), StatutActualite.BROUILLON);
        ActualiteJpaEntity entiteMiseAJour = new ActualiteJpaEntity(ACTUALITE_ID, CategorieActualite.PROJET, "Titre",
                null, AUTEUR_ID, "Auteur", List.of(), StatutActualite.BROUILLON);

        when(actualiteJpaRepository.findById(ACTUALITE_ID)).thenReturn(Optional.of(entiteExistante));
        when(actualiteMapper.updateEntity(entiteExistante, actualite)).thenReturn(entiteMiseAJour);
        when(actualiteJpaRepository.save(entiteMiseAJour)).thenReturn(entiteMiseAJour);
        when(actualiteMapper.toDomain(entiteMiseAJour)).thenReturn(actualite);

        Actualite result = adapter.save(actualite);

        assertThat(result).isEqualTo(actualite);
        verify(actualiteMapper).updateEntity(entiteExistante, actualite);
        verify(actualiteMapper, never()).toNewEntity(any());
    }

    @Test
    @DisplayName("save() crée une nouvelle entité quand aucune entité managée n'existe encore")
    void save_entiteInexistante_creeNouvelle() {
        Actualite actualite = actualiteExistante();
        ActualiteJpaEntity nouvelleEntite = new ActualiteJpaEntity(ACTUALITE_ID, CategorieActualite.PROJET, "Titre",
                null, AUTEUR_ID, "Auteur", List.of(), StatutActualite.BROUILLON);

        when(actualiteJpaRepository.findById(ACTUALITE_ID)).thenReturn(Optional.empty());
        when(actualiteMapper.toNewEntity(actualite)).thenReturn(nouvelleEntite);
        when(actualiteJpaRepository.save(nouvelleEntite)).thenReturn(nouvelleEntite);
        when(actualiteMapper.toDomain(nouvelleEntite)).thenReturn(actualite);

        Actualite result = adapter.save(actualite);

        assertThat(result).isEqualTo(actualite);
        verify(actualiteMapper).toNewEntity(actualite);
        verify(actualiteMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("search() trie sur le champ demandé quand il fait partie des champs autorisés, avec direction ASC")
    void search_champTriAutorise_directionAsc() {
        ActualiteSearchCriteria criteria = ActualiteSearchCriteria.vide();
        PageRequest pageRequest = mock(PageRequest.class);
        when(pageRequest.page()).thenReturn(0);
        when(pageRequest.size()).thenReturn(10);
        when(pageRequest.sortBy()).thenReturn("titre");
        when(pageRequest.direction()).thenReturn(PageRequest.SortDirection.ASC);

        ActualiteJpaEntity entity = new ActualiteJpaEntity(ACTUALITE_ID, CategorieActualite.PROJET, "Titre", null,
                AUTEUR_ID, "Auteur", List.of(), StatutActualite.BROUILLON);
        Page<ActualiteJpaEntity> page = new PageImpl<>(List.of(entity),
                org.springframework.data.domain.PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "titre")), 1);
        when(actualiteJpaRepository.findAll(
                ArgumentMatchers.<Specification<ActualiteJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);
        Actualite actualite = actualiteExistante();
        when(actualiteMapper.toDomain(entity)).thenReturn(actualite);

        PageResult<Actualite> result = adapter.search(criteria, pageRequest);

        assertThat(result.content()).containsExactly(actualite);
        assertThat(result.page()).isZero();
        assertThat(result.totalElements()).isEqualTo(1);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(actualiteJpaRepository).findAll(
                ArgumentMatchers.<Specification<ActualiteJpaEntity>>any(),
                captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("titre");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("search() retombe sur createdAt DESC quand le champ de tri demandé n'est pas autorisé")
    void search_champTriNonAutorise_repliCreatedAtDesc() {
        ActualiteSearchCriteria criteria = ActualiteSearchCriteria.vide();
        PageRequest pageRequest = mock(PageRequest.class);
        when(pageRequest.page()).thenReturn(0);
        when(pageRequest.size()).thenReturn(10);
        when(pageRequest.sortBy()).thenReturn("champInconnu");
        when(pageRequest.direction()).thenReturn(PageRequest.SortDirection.DESC);

        Page<ActualiteJpaEntity> page = new PageImpl<>(List.of());
        when(actualiteJpaRepository.findAll(
                ArgumentMatchers.<Specification<ActualiteJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);

        adapter.search(criteria, pageRequest);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(actualiteJpaRepository).findAll(
                ArgumentMatchers.<Specification<ActualiteJpaEntity>>any(),
                captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    private Actualite actualiteExistante() {
        Instant maintenant = Instant.now();
        return Actualite.reconstruct(ActualiteId.of(ACTUALITE_ID), CategorieActualite.PROJET, "Titre", null,
                List.of(), AUTEUR_ID, "Auteur", List.of(), StatutActualite.BROUILLON, maintenant, maintenant);
    }
}
