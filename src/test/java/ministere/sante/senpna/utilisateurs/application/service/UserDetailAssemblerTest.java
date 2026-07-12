package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailAssembler — assemblage de la représentation UserDetail")
class UserDetailAssemblerTest {

    @Mock
    UserRoleSummaryResolver roleSummaryResolver;
    @Mock
    UserAffectationRepositoryPort userAffectationRepositoryPort;

    UserDetailAssembler sut;

    @BeforeEach
    void setUp() {
        sut = new UserDetailAssembler(roleSummaryResolver, userAffectationRepositoryPort);
    }

    @Test
    @DisplayName("assemble tous les champs de base depuis l'agrégat User")
    void assembleChampsDeBase() {
        User user = UserFixtures.actif();
        when(roleSummaryResolver.resoudre(user.getRoleIds())).thenReturn(Set.of());
        when(userAffectationRepositoryPort.findAffectation(UserFixtures.USER_ID)).thenReturn(Optional.empty());

        UserDetail detail = sut.assembler(user);

        assertThat(detail.id()).isEqualTo(UserFixtures.USER_ID);
        assertThat(detail.nom()).isEqualTo(UserFixtures.NOM);
        assertThat(detail.prenom()).isEqualTo(UserFixtures.PRENOM);
        assertThat(detail.email()).isEqualTo(UserFixtures.EMAIL);
        assertThat(detail.actif()).isTrue();
    }

    @Test
    @DisplayName("téléphone absent → champ telephone null dans le détail")
    void telephoneAbsent_null() {
        User user = UserFixtures.actif();
        when(roleSummaryResolver.resoudre(user.getRoleIds())).thenReturn(Set.of());
        when(userAffectationRepositoryPort.findAffectation(UserFixtures.USER_ID)).thenReturn(Optional.empty());

        assertThat(sut.assembler(user).telephone()).isNull();
    }

    @Test
    @DisplayName("téléphone présent → reporté dans le détail")
    void telephonePresent_reporte() {
        User user = UserFixtures.actifAvecTelephone();
        when(roleSummaryResolver.resoudre(user.getRoleIds())).thenReturn(Set.of());
        when(userAffectationRepositoryPort.findAffectation(UserFixtures.USER_ID)).thenReturn(Optional.empty());

        assertThat(sut.assembler(user).telephone()).isEqualTo(UserFixtures.TELEPHONE);
    }

    @Test
    @DisplayName("affectation présente → entrepotId et structureSanitaireId reportés")
    void affectationPresente_reportee() {
        User user = UserFixtures.actif();
        UUID entrepotId = UUID.randomUUID();
        when(roleSummaryResolver.resoudre(user.getRoleIds())).thenReturn(Set.of());
        when(userAffectationRepositoryPort.findAffectation(UserFixtures.USER_ID))
                .thenReturn(Optional.of(new UserAffectationView(UserFixtures.USER_ID, entrepotId, null)));

        UserDetail detail = sut.assembler(user);

        assertThat(detail.entrepotId()).isEqualTo(entrepotId);
        assertThat(detail.structureSanitaireId()).isNull();
    }

    @Test
    @DisplayName("aucune affectation → entrepotId et structureSanitaireId null")
    void aucuneAffectation_champsNull() {
        User user = UserFixtures.actif();
        when(roleSummaryResolver.resoudre(user.getRoleIds())).thenReturn(Set.of());
        when(userAffectationRepositoryPort.findAffectation(UserFixtures.USER_ID)).thenReturn(Optional.empty());

        UserDetail detail = sut.assembler(user);

        assertThat(detail.entrepotId()).isNull();
        assertThat(detail.structureSanitaireId()).isNull();
    }
}
