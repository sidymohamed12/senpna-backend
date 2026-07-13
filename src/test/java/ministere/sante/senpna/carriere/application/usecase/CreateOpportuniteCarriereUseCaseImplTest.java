package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereCommandMapper;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CreateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOpportuniteCarriereUseCaseImpl — création d'une opportunité de carrière")
class CreateOpportuniteCarriereUseCaseImplTest {

    @Mock
    OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    OpportuniteCarriereCommandMapper commandMapper;
    @Mock
    OpportuniteCarriereDetailAssembler assembler;

    CreateOpportuniteCarriereUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CreateOpportuniteCarriereUseCaseImpl(opportuniteCarriereRepositoryPort,
                userManagementRepositoryPort, commandMapper, assembler);
    }

    private CreateOpportuniteCarriereCommand commande() {
        return new CreateOpportuniteCarriereCommand(UserFixtures.USER_ID, "Developpeur", "Entreprise X", "Desc",
                null, "Dakar", "CDI", LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), "rh@x.sn");
    }

    @Test
    @DisplayName("auteur introuvable → UserNotFoundException")
    void auteurIntrouvable_leveException() {
        when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

        var command = commande();
        assertThatThrownBy(() -> sut.creer(command)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("construit le nom d'auteur à partir du prénom et du nom, crée en statut BROUILLON")
    void construitNomAuteurEtCreeEnBrouillon() {
        User auteur = UserFixtures.actif();
        when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(auteur));
        when(commandMapper.versTypeContrat("CDI")).thenReturn(TypeContrat.CDI);
        when(opportuniteCarriereRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assembler.assembler(any())).thenReturn(null);

        sut.creer(commande());

        ArgumentCaptor<OpportuniteCarriere> captor = ArgumentCaptor.forClass(OpportuniteCarriere.class);
        verify(opportuniteCarriereRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getAuteurNom()).isEqualTo(UserFixtures.PRENOM + " " + UserFixtures.NOM);
        assertThat(captor.getValue().getStatut().name()).isEqualTo("BROUILLON");
    }
}
