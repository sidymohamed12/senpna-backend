package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.AuthTokenFactory;
import ministere.sante.senpna.auth.application.service.UserAffectationResolver;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;
import ministere.sante.senpna.auth.domain.exception.CompteInactifException;
import ministere.sante.senpna.auth.domain.exception.CompteVerrouilleException;
import ministere.sante.senpna.auth.domain.exception.InvalidCredentialsException;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCaseImpl")
class LoginUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordEncoderPort passwordEncoderPort;
    @Mock
    private AuthTokenFactory authTokenFactory;
    @Mock
    private UserRoleResolver userRoleResolver;
    @Mock
    private UserAffectationResolver userAffectationResolver;

    @InjectMocks
    private LoginUseCaseImpl sut;

    private static final String EMAIL = UserFixtures.EMAIL;
    private static final String PASSWORD = UserFixtures.PASSWORD_BRUT;
    private static final Set<String> ROLE_CODES = Set.of("GESTIONNAIRE_PNA");
    private static final AuthTokens TOKENS = new AuthTokens("access.token", "refresh.token", 3600L);

    @BeforeEach
    void configurerMocksParDefaut() {
        // Stubs en mode lenient : utilisés uniquement dans les tests du chemin succès.
        // Les tests d'erreur (mot de passe faux, compte verrouillé…) ne les consomment
        // pas
        // et Mockito strict les signalerait sinon.
        lenient().when(userRoleResolver.resoudreCodes(any())).thenReturn(ROLE_CODES);
        lenient().when(authTokenFactory.build(any(User.class), any())).thenReturn(TOKENS);
        lenient().when(userAffectationResolver.resoudre(any()))
                .thenReturn(new UserAffectationView(UserFixtures.USER_ID, null, null, null));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Succès
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Connexion réussie")
    class Succes {

        @BeforeEach
        void setup() {
            when(userRepositoryPort.findByEmail(any()))
                    .thenReturn(Optional.of(UserFixtures.actif()));
            when(passwordEncoderPort.correspond(any(), any()))
                    .thenReturn(true);
            when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("retourne les tokens et le résumé utilisateur")
        void login_retourne_result_complet() {
            LoginResult result = sut.login(new LoginCommand(EMAIL, PASSWORD));

            assertThat(result.tokens().accessToken()).isEqualTo("access.token");
            assertThat(result.tokens().refreshToken()).isEqualTo("refresh.token");
            assertThat(result.tokens().expiresInSeconds()).isEqualTo(3600L);
            assertThat(result.user().email()).isEqualTo(EMAIL);
            assertThat(result.user().nom()).isEqualTo(UserFixtures.NOM);
            assertThat(result.user().prenom()).isEqualTo(UserFixtures.PRENOM);
            assertThat(result.user().roles()).isEqualTo(ROLE_CODES);
        }

        @Test
        @DisplayName("réinitialise les échecs de connexion avant de sauvegarder")
        void login_reinitialise_echecs() {
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            sut.login(new LoginCommand(EMAIL, PASSWORD));

            verify(userRepositoryPort).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getTentativesEchecConnexion()).isZero();
            assertThat(userCaptor.getValue().getVerrouilleJusqua()).isNull();
        }

        @Test
        @DisplayName("construit les tokens avec le bon utilisateur et les bons rôles")
        void login_appelle_factory_avec_user_et_roles() {
            sut.login(new LoginCommand(EMAIL, PASSWORD));

            verify(authTokenFactory).build(any(User.class), eq(ROLE_CODES));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Utilisateur introuvable
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Utilisateur introuvable")
    class UserIntrouvable {

        @Test
        @DisplayName("lève InvalidCredentialsException (pas UserNotFoundException — anti-enumération)")
        void login_user_absent_leve_invalid_credentials() {
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());

            var loginCommand = new LoginCommand(EMAIL, PASSWORD);
            assertThatThrownBy(() -> sut.login(loginCommand))
                    .isInstanceOf(InvalidCredentialsException.class);

            verifyNoInteractions(passwordEncoderPort, authTokenFactory);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Mauvais mot de passe
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Mot de passe incorrect")
    class MauvaisMotDePasse {

        @BeforeEach
        void setup() {
            when(userRepositoryPort.findByEmail(any()))
                    .thenReturn(Optional.of(UserFixtures.actif()));
            when(passwordEncoderPort.correspond(anyString(), anyString())).thenReturn(false);
            when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("lève InvalidCredentialsException")
        void login_mauvaisPassword_leve_invalid_credentials() {
            var loginCommand = new LoginCommand(EMAIL, "mauvais");
            assertThatThrownBy(() -> sut.login(loginCommand))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("incrémente le compteur d'échecs et sauvegarde")
        void login_mauvaisPassword_incremente_echecs() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            var loginCommand = new LoginCommand(EMAIL, "mauvais");
            assertThatThrownBy(() -> sut.login(loginCommand))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getTentativesEchecConnexion()).isEqualTo(1);
        }

        @Test
        @DisplayName("verrouille le compte après 5 échecs consécutifs")
        void login_5echecs_verrouille_compte() {
            // Simuler un utilisateur avec déjà 4 échecs
            when(userRepositoryPort.findByEmail(any()))
                    .thenReturn(Optional.of(UserFixtures.avecEchecs(4)));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            var loginCommand = new LoginCommand(EMAIL, "mauvais");
            assertThatThrownBy(() -> sut.login(loginCommand))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepositoryPort).save(captor.capture());
            User sauvegarde = captor.getValue();
            assertThat(sauvegarde.getTentativesEchecConnexion()).isEqualTo(5);
            assertThat(sauvegarde.estVerrouille()).isTrue();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Compte verrouillé
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Compte verrouillé")
    class CompteVerrouille {

        @Test
        @DisplayName("lève CompteVerrouilleException sans vérifier le mot de passe")
        void login_compteVerrouille_leve_exception_sans_verif_password() {
            when(userRepositoryPort.findByEmail(any()))
                    .thenReturn(Optional.of(UserFixtures.verrouille()));

            var loginCommand = new LoginCommand(EMAIL, PASSWORD);
            assertThatThrownBy(() -> sut.login(loginCommand))
                    .isInstanceOf(CompteVerrouilleException.class);

            verifyNoInteractions(passwordEncoderPort);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Compte inactif
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Compte inactif")
    class CompteInactif {

        @Test
        @DisplayName("lève CompteInactifException après vérification du mot de passe")
        void login_compteInactif_leve_exception() {
            when(userRepositoryPort.findByEmail(any()))
                    .thenReturn(Optional.of(UserFixtures.inactif()));
            when(passwordEncoderPort.correspond(any(), any())) // ← any() ici aussi
                    .thenReturn(true);

            var loginCommand = new LoginCommand(EMAIL, PASSWORD);
            assertThatThrownBy(() -> sut.login(loginCommand))
                    .isInstanceOf(CompteInactifException.class);

            verifyNoInteractions(authTokenFactory);
        }
    }
}