package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.RoleJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.mapper.UserMapper;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.RoleJpaRepository;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.UserJpaRepository;
import ministere.sante.senpna.shared.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ UserManagementRepositoryAdapter.class, UserRepositoryAdapter.class, RoleQueryAdapter.class,
        UserMapper.class })
@DisplayName("Adaptateurs de persistence auth — H2")
class AuthPersistenceAdaptersTest {

    @Autowired
    UserJpaRepository userJpaRepository;
    @Autowired
    RoleJpaRepository roleJpaRepository;
    @Autowired
    UserManagementRepositoryAdapter userManagementRepositoryAdapter;
    @Autowired
    UserRepositoryAdapter userRepositoryAdapter;
    @Autowired
    RoleQueryAdapter roleQueryAdapter;
    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        User actifDiallo = UserFixtures.actif();
        User inactifAutre = User.creer(new User.CreationCommand(
                ministere.sante.senpna.shared.domain.valueobject.Nom.of("Sow"),
                ministere.sante.senpna.shared.domain.valueobject.Prenom.of("Ousmane"),
                Email.of("ousmane.sow@sante.gouv.sn"),
                null,
                ministere.sante.senpna.shared.domain.valueobject.HashedPassword.of("hash"),
                java.util.Set.of(UserFixtures.ROLE_GESTIONNAIRE_PNA_ID)));
        inactifAutre.desactiver();

        userManagementRepositoryAdapter.save(actifDiallo);
        userManagementRepositoryAdapter.save(inactifAutre);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("UserManagementRepositoryAdapter.search()")
    class Search {

        @Test
        @DisplayName("filtre par statut actif")
        void filtreParActif() {
            PageResult<User> result = userManagementRepositoryAdapter.search(
                    new UserSearchCriteria(null, true, null), PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(u -> u.getEmail().value())
                    .containsExactly(UserFixtures.EMAIL);
        }

        @Test
        @DisplayName("filtre par texte sur le nom, insensible à la casse")
        void filtreParTexte() {
            PageResult<User> result = userManagementRepositoryAdapter.search(
                    new UserSearchCriteria("sow", null, null), PageRequest.of(0, 20, null, null));

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getNom().getValue()).isEqualTo("Sow");
        }

        @Test
        @DisplayName("filtre par rôle possédé")
        void filtreParRole() {
            PageResult<User> result = userManagementRepositoryAdapter.search(
                    new UserSearchCriteria(null, null, UserFixtures.ROLE_GESTIONNAIRE_PNA_ID),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("aucun critère → tous les utilisateurs")
        void aucunCritere_tous() {
            PageResult<User> result = userManagementRepositoryAdapter.search(UserSearchCriteria.vide(),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("UserRepositoryAdapter")
    class UserRepositoryAdapterTests {

        @Test
        @DisplayName("findByEmail() retrouve l'utilisateur par email")
        void findByEmail_retrouve() {
            Optional<User> result = userRepositoryAdapter.findByEmail(Email.of(UserFixtures.EMAIL));

            assertThat(result).isPresent();
            assertThat(result.get().getId().getValue()).isEqualTo(UserFixtures.USER_ID);
        }

        @Test
        @DisplayName("findById() retrouve l'utilisateur par identifiant")
        void findById_retrouve() {
            Optional<User> result = userRepositoryAdapter.findById(UserId.of(UserFixtures.USER_ID));

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("existsByEmail() reflète l'existence en base")
        void existsByEmail() {
            assertThat(userRepositoryAdapter.existsByEmail(Email.of(UserFixtures.EMAIL))).isTrue();
            assertThat(userRepositoryAdapter.existsByEmail(Email.of("inconnu@sante.gouv.sn"))).isFalse();
        }
    }

    @Nested
    @DisplayName("RoleQueryAdapter")
    class RoleQueryAdapterTests {

        @Test
        @DisplayName("findAll() renvoie une projection par rôle persisté")
        void findAll_projections() {
            roleJpaRepository.save(new RoleJpaEntity("ADMIN_PNA", "Administrateur national"));
            entityManager.flush();
            entityManager.clear();

            List<RoleProjection> result = roleQueryAdapter.findAll();

            assertThat(result).extracting(RoleProjection::code).contains("ADMIN_PNA");
        }

        @Test
        @DisplayName("findByCode() retrouve le rôle par son code")
        void findByCode_retrouve() {
            roleJpaRepository.save(new RoleJpaEntity("PHARMACIEN_PRA", "Pharmacien PRA"));
            entityManager.flush();
            entityManager.clear();

            assertThat(roleQueryAdapter.findByCode("PHARMACIEN_PRA")).isPresent();
            assertThat(roleQueryAdapter.findByCode("INEXISTANT")).isEmpty();
        }

        @Test
        @DisplayName("findNomById() sur un identifiant inconnu → NotFoundException")
        void findNomById_inconnu_leveException() {
            var id = UUID.randomUUID();
            assertThatThrownBy(() -> roleQueryAdapter.findNomById(id))
                    .isInstanceOf(SenPnaException.class);
        }
    }
}
