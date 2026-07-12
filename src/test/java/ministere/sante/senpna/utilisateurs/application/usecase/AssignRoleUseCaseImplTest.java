package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.AssignRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleDejaAssigneException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;

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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignRoleUseCaseImpl — attribution d'un rôle")
class AssignRoleUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    RoleQueryPort roleQueryPort;
    @Mock
    UserHierarchyGuard userHierarchyGuard;
    @Mock
    UserDetailAssembler userDetailAssembler;

    AssignRoleUseCaseImpl sut;

    UUID roleId;

    @BeforeEach
    void setUp() {
        sut = new AssignRoleUseCaseImpl(userManagementRepositoryPort, roleQueryPort, userHierarchyGuard,
                userDetailAssembler);
        roleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("rôle inconnu → RoleIntrouvableException, aucune lecture de l'utilisateur")
    void roleInconnu_leveException() {
        when(roleQueryPort.existsById(roleId)).thenReturn(false);

        var assignRoleCommand = new AssignRoleCommand(UserFixtures.USER_ID, UUID.randomUUID(), roleId);
        assertThatThrownBy(() -> sut.assigner(assignRoleCommand))
                .isInstanceOf(RoleIntrouvableException.class);

        verify(userManagementRepositoryPort, never()).findById(any());
    }

    @Test
    @DisplayName("utilisateur introuvable → UserNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(roleQueryPort.existsById(roleId)).thenReturn(true);
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.empty());

        var assignRoleCommand = new AssignRoleCommand(UserFixtures.USER_ID, UUID.randomUUID(), roleId);
        assertThatThrownBy(() -> sut.assigner(assignRoleCommand))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("rôle déjà attribué → RoleDejaAssigneException")
    void roleDejaAttribue_leveException() {
        User user = UserFixtures.actif();
        when(roleQueryPort.existsById(any())).thenReturn(true);
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));

        var assignRoleCommand = new AssignRoleCommand(UserFixtures.USER_ID, UUID.randomUUID(),
                UserFixtures.ROLE_GESTIONNAIRE_PNA_ID);
        assertThatThrownBy(() -> sut.assigner(assignRoleCommand))
                .isInstanceOf(RoleDejaAssigneException.class);
    }

    @Test
    @DisplayName("attribution valide → rôle ajouté et utilisateur sauvegardé")
    void attributionValide_ajouteEtSauvegarde() {
        User user = UserFixtures.actif();
        when(roleQueryPort.existsById(roleId)).thenReturn(true);
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(userManagementRepositoryPort.save(user)).thenReturn(user);

        sut.assigner(new AssignRoleCommand(UserFixtures.USER_ID, UUID.randomUUID(), roleId));

        assertThat(user.getRoleIds()).contains(roleId);
    }
}
