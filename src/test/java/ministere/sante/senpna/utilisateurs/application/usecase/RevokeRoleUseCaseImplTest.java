package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RevokeRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.exception.DernierRoleException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleNonAssigneException;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevokeRoleUseCaseImpl — retrait d'un rôle")
class RevokeRoleUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    UserHierarchyGuard userHierarchyGuard;
    @Mock
    UserDetailAssembler userDetailAssembler;

    RevokeRoleUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new RevokeRoleUseCaseImpl(userManagementRepositoryPort, userHierarchyGuard, userDetailAssembler);
    }

    @Test
    @DisplayName("rôle non attribué à l'utilisateur → RoleNonAssigneException")
    void roleNonAttribue_leveException() {
        User user = UserFixtures.actif();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sut.retirer(
                new RevokeRoleCommand(UserFixtures.USER_ID, UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(RoleNonAssigneException.class);
    }

    @Test
    @DisplayName("retrait du dernier rôle → DernierRoleException")
    void dernierRole_leveException() {
        User user = UserFixtures.actif(); // possède exactement 1 rôle : ROLE_GESTIONNAIRE_PNA_ID
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sut.retirer(new RevokeRoleCommand(UserFixtures.USER_ID, UUID.randomUUID(),
                UserFixtures.ROLE_GESTIONNAIRE_PNA_ID)))
                .isInstanceOf(DernierRoleException.class);
    }

    @Test
    @DisplayName("retrait valide (utilisateur multi-rôles) → rôle retiré et sauvegardé")
    void retraitValide_retireEtSauvegarde() {
        User user = UserFixtures.multiRoles();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(userManagementRepositoryPort.save(user)).thenReturn(user);

        sut.retirer(new RevokeRoleCommand(UserFixtures.USER_ID, UUID.randomUUID(),
                UserFixtures.ROLE_PHARMACIEN_PRA_ID));

        assertThat(user.getRoleIds()).doesNotContain(UserFixtures.ROLE_PHARMACIEN_PRA_ID);
        assertThat(user.getRoleIds()).contains(UserFixtures.ROLE_GESTIONNAIRE_PNA_ID);
    }
}
