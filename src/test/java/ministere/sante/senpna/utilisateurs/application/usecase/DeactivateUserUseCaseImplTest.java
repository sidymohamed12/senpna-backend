package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.DeactivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.exception.AutoDesactivationInterditeException;

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
@DisplayName("DeactivateUserUseCaseImpl — désactivation d'un compte utilisateur")
class DeactivateUserUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    UserHierarchyGuard userHierarchyGuard;
    @Mock
    UserDetailAssembler userDetailAssembler;

    DeactivateUserUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new DeactivateUserUseCaseImpl(userManagementRepositoryPort, userHierarchyGuard, userDetailAssembler);
    }

    @Test
    @DisplayName("désactive le compte et sauvegarde")
    void desactive_etSauvegarde() {
        User user = UserFixtures.actif();
        UUID acteurId = UUID.randomUUID();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(userManagementRepositoryPort.save(user)).thenReturn(user);

        sut.desactiver(new DeactivateUserCommand(UserFixtures.USER_ID, acteurId));

        assertThat(user.isActif()).isFalse();
    }

    @Test
    @DisplayName("un acteur ne peut pas se désactiver lui-même → AutoDesactivationInterditeException")
    void autoDesactivation_interdite() {
        assertThatThrownBy(() -> sut.desactiver(
                new DeactivateUserCommand(UserFixtures.USER_ID, UserFixtures.USER_ID)))
                .isInstanceOf(AutoDesactivationInterditeException.class);

        verify(userManagementRepositoryPort, never()).findById(any());
    }

    @Test
    @DisplayName("utilisateur introuvable → UserNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.desactiver(new DeactivateUserCommand(UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(UserNotFoundException.class);
    }
}
