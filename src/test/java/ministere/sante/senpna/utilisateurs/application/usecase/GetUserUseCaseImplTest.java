package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.GetUserQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserUseCaseImpl — consultation d'un compte utilisateur")
class GetUserUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    UserDetailAssembler userDetailAssembler;

    GetUserUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GetUserUseCaseImpl(userManagementRepositoryPort, userDetailAssembler);
    }

    @Test
    @DisplayName("utilisateur trouvé → renvoie le détail assemblé")
    void utilisateurTrouve_renvoieDetail() {
        User user = UserFixtures.actif();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        UserDetail detail = new UserDetail(UserFixtures.USER_ID, "N", "P", "e", null, true, java.util.Set.of(),
                null, null, null, null, null);
        when(userDetailAssembler.assembler(user)).thenReturn(detail);

        assertThat(sut.obtenir(new GetUserQuery(UserFixtures.USER_ID))).isSameAs(detail);
    }

    @Test
    @DisplayName("utilisateur introuvable → UserNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.empty());

        var getUserQuery = new GetUserQuery(UserFixtures.USER_ID);
        assertThatThrownBy(() -> sut.obtenir(getUserQuery))
                .isInstanceOf(UserNotFoundException.class);
    }
}
