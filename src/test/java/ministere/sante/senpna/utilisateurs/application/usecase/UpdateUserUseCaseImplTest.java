package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UpdateUserCommand;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserUseCaseImpl — modification d'un compte utilisateur")
class UpdateUserUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    UserHierarchyGuard userHierarchyGuard;
    @Mock
    UserDetailAssembler userDetailAssembler;

    UpdateUserUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new UpdateUserUseCaseImpl(userManagementRepositoryPort, userHierarchyGuard, userDetailAssembler);
    }

    @Test
    @DisplayName("met à jour le nom, prénom et téléphone puis sauvegarde")
    void metAJourEtSauvegarde() {
        User user = UserFixtures.actif();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(userManagementRepositoryPort.save(user)).thenReturn(user);

        sut.modifier(new UpdateUserCommand(UserFixtures.USER_ID, UUID.randomUUID(), "Fall", "Awa", "+221709998877"));

        assertThat(user.getNom().getValue()).isEqualTo("Fall");
        assertThat(user.getPrenom().getValue()).isEqualTo("Awa");
        assertThat(user.getTelephone().value()).isEqualTo("+221709998877");
    }

    @Test
    @DisplayName("téléphone vide/blanc → retiré (mis à null)")
    void telephoneVide_retire() {
        User user = UserFixtures.actifAvecTelephone();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(userManagementRepositoryPort.save(user)).thenReturn(user);

        sut.modifier(new UpdateUserCommand(UserFixtures.USER_ID, UUID.randomUUID(), "Fall", "Awa", "   "));

        assertThat(user.getTelephone()).isNull();
    }

    @Test
    @DisplayName("utilisateur introuvable → UserNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.modifier(
                new UpdateUserCommand(UUID.randomUUID(), UUID.randomUUID(), "Fall", "Awa", null)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
