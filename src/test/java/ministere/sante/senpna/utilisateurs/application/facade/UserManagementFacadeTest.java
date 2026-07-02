package ministere.sante.senpna.utilisateurs.application.facade;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.*;
import ministere.sante.senpna.utilisateurs.domain.port.in.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link UserManagementFacade} — vérifie que chaque
 * méthode délègue à l'use case correspondant avec la commande/requête
 * fournie, sans logique métier propre.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserManagementFacade")
class UserManagementFacadeTest {

    @Mock
    CreateUserUseCase createUserUseCase;
    @Mock
    GetUserUseCase getUserUseCase;
    @Mock
    ListUsersUseCase listUsersUseCase;
    @Mock
    UpdateUserUseCase updateUserUseCase;
    @Mock
    ActivateUserUseCase activateUserUseCase;
    @Mock
    DeactivateUserUseCase deactivateUserUseCase;
    @Mock
    AssignRoleUseCase assignRoleUseCase;
    @Mock
    RevokeRoleUseCase revokeRoleUseCase;

    @InjectMocks
    UserManagementFacade sut;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ROLE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UserDetail DETAIL = new UserDetail(USER_ID, "Diallo", "Mamadou",
            "mamadou.diallo@sante.gouv.sn", null, true, Set.of(), Instant.now(), Instant.now());

    @Test
    @DisplayName("creer() délègue à CreateUserUseCase")
    void creer_delegue() {
        CreateUserCommand command = new CreateUserCommand(UUID.randomUUID(), "Diallo", "Mamadou",
                "mamadou.diallo@sante.gouv.sn", null, Set.of(ROLE_ID));
        CreatedUser createdUser = new CreatedUser(DETAIL, "TempPass1!");
        when(createUserUseCase.creer(command)).thenReturn(createdUser);

        CreatedUser result = sut.creer(command);

        assertThat(result).isEqualTo(createdUser);
        verify(createUserUseCase).creer(command);
    }

    @Test
    @DisplayName("obtenir() délègue à GetUserUseCase")
    void obtenir_delegue() {
        GetUserQuery query = new GetUserQuery(USER_ID);
        when(getUserUseCase.obtenir(query)).thenReturn(DETAIL);

        UserDetail result = sut.obtenir(query);

        assertThat(result).isEqualTo(DETAIL);
        verify(getUserUseCase).obtenir(query);
    }

    @Test
    @DisplayName("lister() délègue à ListUsersUseCase")
    void lister_delegue() {
        ListUsersQuery query = new ListUsersQuery(null, null, null, null, null, null, null);
        UserPage page = new UserPage(java.util.List.of(DETAIL), 0, 20, 1, 1);
        when(listUsersUseCase.lister(query)).thenReturn(page);

        UserPage result = sut.lister(query);

        assertThat(result).isEqualTo(page);
        verify(listUsersUseCase).lister(query);
    }

    @Test
    @DisplayName("modifier() délègue à UpdateUserUseCase")
    void modifier_delegue() {
        UpdateUserCommand command = new UpdateUserCommand(USER_ID, UUID.randomUUID(), "Diallo", "Mamadou", null);
        when(updateUserUseCase.modifier(command)).thenReturn(DETAIL);

        UserDetail result = sut.modifier(command);

        assertThat(result).isEqualTo(DETAIL);
        verify(updateUserUseCase).modifier(command);
    }

    @Test
    @DisplayName("activer() délègue à ActivateUserUseCase")
    void activer_delegue() {
        ActivateUserCommand command = new ActivateUserCommand(USER_ID, UUID.randomUUID());
        when(activateUserUseCase.activer(command)).thenReturn(DETAIL);

        UserDetail result = sut.activer(command);

        assertThat(result).isEqualTo(DETAIL);
        verify(activateUserUseCase).activer(command);
    }

    @Test
    @DisplayName("desactiver() délègue à DeactivateUserUseCase")
    void desactiver_delegue() {
        DeactivateUserCommand command = new DeactivateUserCommand(USER_ID, UUID.randomUUID());
        when(deactivateUserUseCase.desactiver(command)).thenReturn(DETAIL);

        UserDetail result = sut.desactiver(command);

        assertThat(result).isEqualTo(DETAIL);
        verify(deactivateUserUseCase).desactiver(command);
    }

    @Test
    @DisplayName("assignerRole() délègue à AssignRoleUseCase")
    void assignerRole_delegue() {
        AssignRoleCommand command = new AssignRoleCommand(USER_ID, UUID.randomUUID(), ROLE_ID);
        when(assignRoleUseCase.assigner(command)).thenReturn(DETAIL);

        UserDetail result = sut.assignerRole(command);

        assertThat(result).isEqualTo(DETAIL);
        verify(assignRoleUseCase).assigner(command);
    }

    @Test
    @DisplayName("retirerRole() délègue à RevokeRoleUseCase")
    void retirerRole_delegue() {
        RevokeRoleCommand command = new RevokeRoleCommand(USER_ID, UUID.randomUUID(), ROLE_ID);
        when(revokeRoleUseCase.retirer(command)).thenReturn(DETAIL);

        UserDetail result = sut.retirerRole(command);

        assertThat(result).isEqualTo(DETAIL);
        verify(revokeRoleUseCase).retirer(command);
    }
}
