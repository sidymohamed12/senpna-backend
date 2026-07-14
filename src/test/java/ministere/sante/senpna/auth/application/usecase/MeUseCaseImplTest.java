package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserAffectationResolver;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.MeQuery;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeUseCaseImpl — profil de l'utilisateur courant")
class MeUseCaseImplTest {

    @Mock
    UserRepositoryPort userRepositoryPort;
    @Mock
    UserRoleResolver userRoleResolver;
    @Mock
    UserAffectationResolver userAffectationResolver;

    MeUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new MeUseCaseImpl(userRepositoryPort, userRoleResolver, userAffectationResolver);
    }

    @Test
    @DisplayName("utilisateur introuvable → UserNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.empty());

        var meQuery = new MeQuery(UserFixtures.EMAIL);
        assertThatThrownBy(() -> sut.me(meQuery))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("assemble le résumé avec rôles et affectation résolus")
    void assembleResumeComplet() {
        User user = UserFixtures.actif();
        UUID entrepotId = UUID.randomUUID();
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));
        when(userRoleResolver.resoudreCodes(user.getRoleIds())).thenReturn(Set.of("ADMIN_PNA"));
        when(userAffectationResolver.resoudre(UserFixtures.USER_ID))
                .thenReturn(new UserAffectationView(UserFixtures.USER_ID, entrepotId, null, null));

        UserSummary summary = sut.me(new MeQuery(UserFixtures.EMAIL));

        assertThat(summary.id()).isEqualTo(UserFixtures.USER_ID);
        assertThat(summary.nom()).isEqualTo(UserFixtures.NOM);
        assertThat(summary.roles()).containsExactly("ADMIN_PNA");
        assertThat(summary.entrepotId()).isEqualTo(entrepotId);
    }
}
