package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.mapper.UserMapper;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.UserJpaRepository;
import ministere.sante.senpna.shared.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link UserManagementRepositoryAdapter} —
 * vérifient la délégation au repository Spring Data, la construction de la
 * pagination et la traduction domaine ⇆ persistance, sans contexte Spring
 * (contrairement à {@code AuthPersistenceAdaptersTest}, taggé
 * {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserManagementRepositoryAdapter — unitaire")
class UserManagementRepositoryAdapterUnitTest {

    @Mock
    UserJpaRepository userJpaRepository;

    @Mock
    UserMapper userMapper;

    UserManagementRepositoryAdapter adapter;

    User user = UserFixtures.actif();
    UserJpaEntity entity = UserJpaEntity.builder()
        .id(UserFixtures.USER_ID)
        .nom(UserFixtures.NOM)
        .prenom(UserFixtures.PRENOM)
        .email(UserFixtures.EMAIL)
        .telephone(null)
        .passwordHash(UserFixtures.PASSWORD_HASH)
        .actif(true)
        .roleIds(user.getRoleIds())
        .tentativesEchecConnexion(0)
        .verrouilleJusqua(null)
        .build();

    @BeforeEach
    void setUp() {
        adapter = new UserManagementRepositoryAdapter(userJpaRepository, userMapper);
    }

    @Test
    @DisplayName("findById() délègue au repository et mappe la présence")
    void findById_present() {
        when(userJpaRepository.findById(UserFixtures.USER_ID)).thenReturn(Optional.of(entity));
        when(userMapper.toDomain(entity)).thenReturn(user);

        assertThat(adapter.findById(UserId.of(UserFixtures.USER_ID))).contains(user);
    }

    @Test
    @DisplayName("findById() renvoie vide si absent")
    void findById_absent() {
        when(userJpaRepository.findById(UserFixtures.USER_ID)).thenReturn(Optional.empty());

        assertThat(adapter.findById(UserId.of(UserFixtures.USER_ID))).isEmpty();
    }

    @Test
    @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
    void save() {
        when(userMapper.toEntity(user)).thenReturn(entity);
        when(userJpaRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDomain(entity)).thenReturn(user);

        User result = adapter.save(user);

        assertThat(result).isEqualTo(user);
        verify(userJpaRepository).save(entity);
    }

    @Test
    @DisplayName("existsByEmail() délègue au repository")
    void existsByEmail() {
        when(userJpaRepository.existsByEmail(UserFixtures.EMAIL)).thenReturn(true);

        assertThat(adapter.existsByEmail(Email.of(UserFixtures.EMAIL))).isTrue();
    }

    @Test
    @DisplayName("search() construit la pagination et mappe le contenu")
    void search() {
        Page<UserJpaEntity> page = new PageImpl<>(List.of(entity));
        when(userJpaRepository.findAll(
                Mockito.<Specification<UserJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);

        when(userMapper.toDomain(entity)).thenReturn(user);

        PageResult<User> result = adapter.search(new UserSearchCriteria("diallo", true, UUID.randomUUID()),
                new PageRequest(0, 20, "email", PageRequest.SortDirection.ASC));

        assertThat(result.content()).containsExactly(user);
    }

    @Test
    @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
    void search_champTriNonAutorise() {
        Page<UserJpaEntity> page = new PageImpl<>(List.of());
        when(userJpaRepository.findAll(
                Mockito.<Specification<UserJpaEntity>>any(),
                any(Pageable.class)))
                .thenReturn(page);

        PageResult<User> result = adapter.search(UserSearchCriteria.vide(),
                new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

        assertThat(result.content()).isEmpty();
    }
}
