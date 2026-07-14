package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ActivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivateUserUseCaseImpl — activation d'un compte utilisateur")
class ActivateUserUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    UserHierarchyGuard userHierarchyGuard;
    @Mock
    UserDetailAssembler userDetailAssembler;

    ActivateUserUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ActivateUserUseCaseImpl(userManagementRepositoryPort, userHierarchyGuard, userDetailAssembler);
    }

    @Test
    @DisplayName("active le compte, réinitialise les échecs de connexion et sauvegarde")
    void active_reinitialiseEchecsEtSauvegarde() {
        User user = UserFixtures.verrouille();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(userManagementRepositoryPort.save(user)).thenReturn(user);
        UserDetail detail = new UserDetail(UserFixtures.USER_ID, "N", "P", "e", null, true, java.util.Set.of(),
                null, null, null, null, null);
        when(userDetailAssembler.assembler(user)).thenReturn(detail);

        UserDetail result = sut.activer(new ActivateUserCommand(UserFixtures.USER_ID, UUID.randomUUID()));

        assertThat(user.isActif()).isTrue();
        assertThat(user.getTentativesEchecConnexion()).isZero();
        assertThat(result).isSameAs(detail);
    }

    @Test
    @DisplayName("vérifie la hiérarchie de gestion avant activation")
    void verifieHierarchie() {
        User user = UserFixtures.inactif();
        UUID acteurId = UUID.randomUUID();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(userManagementRepositoryPort.save(any())).thenReturn(user);
        when(userDetailAssembler.assembler(any())).thenReturn(null);

        sut.activer(new ActivateUserCommand(UserFixtures.USER_ID, acteurId));

        verify(userHierarchyGuard).verifierGestionAutorisee(acteurId, user.getRoleIds());
    }

    @Test
    @DisplayName("utilisateur introuvable → UserNotFoundException, aucune sauvegarde")
    void utilisateurIntrouvable_leveException() {
        when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

        var activateUserCommand = new ActivateUserCommand(UUID.randomUUID(), UUID.randomUUID());
        assertThatThrownBy(() -> sut.activer(activateUserCommand))
                .isInstanceOf(UserNotFoundException.class);

        verify(userManagementRepositoryPort, never()).save(any());
    }
}
