package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.mapper.UserMapper;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.UserJpaRepository;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link UserRepositoryAdapter} — vérifient la
 * délégation au repository Spring Data et la traduction domaine ⇆
 * persistance, sans contexte Spring (contrairement à
 * {@code AuthPersistenceAdaptersTest}, taggé {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepositoryAdapter — unitaire")
class UserRepositoryAdapterUnitTest {

    @Mock
    UserJpaRepository userJpaRepository;

    @Mock
    UserMapper userMapper;

    UserRepositoryAdapter adapter;

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
        adapter = new UserRepositoryAdapter(userJpaRepository, userMapper);
    }

    @Test
    @DisplayName("findByEmail() délègue au repository et mappe la présence")
    void findByEmail_present() {
        when(userJpaRepository.findByEmail(UserFixtures.EMAIL)).thenReturn(Optional.of(entity));
        when(userMapper.toDomain(entity)).thenReturn(user);

        assertThat(adapter.findByEmail(Email.of(UserFixtures.EMAIL))).contains(user);
    }

    @Test
    @DisplayName("findByEmail() renvoie vide si absent")
    void findByEmail_absent() {
        when(userJpaRepository.findByEmail(UserFixtures.EMAIL)).thenReturn(Optional.empty());

        assertThat(adapter.findByEmail(Email.of(UserFixtures.EMAIL))).isEmpty();
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
}
