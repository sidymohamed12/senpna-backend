package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ValiderCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutCommandeAchatInvalideException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValiderCommandeAchatUseCaseImpl")
class ValiderCommandeAchatUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    ValiderCommandeAchatUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ValiderCommandeAchatUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
    }

    private CommandeAchat commande() {
        LigneCommandeAchat ligne = LigneCommandeAchat.creer(MedicamentId.generate(), ConditionnementId.generate(),
                BigDecimal.TEN, BigDecimal.TEN);
        return CommandeAchat.creer("BC-1", FournisseurId.generate(), EntrepotId.generate(), List.of(ligne), null);
    }

    @Test
    @DisplayName("commande introuvable → CommandeAchatIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(commandeAchatRepositoryPort.findById(CommandeAchatId.of(id))).thenReturn(Optional.empty());

        var command = new ValiderCommandeAchatCommand(id);
        assertThatThrownBy(() -> sut.valider(command)).isInstanceOf(CommandeAchatIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal → VALIDEE")
    void casNominal_passeValidee() {
        CommandeAchat commande = commande();
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.valider(new ValiderCommandeAchatCommand(commande.getId().getValue()));

        assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.VALIDEE);
    }

    @Test
    @DisplayName("commande déjà validée → TransitionStatutCommandeAchatInvalideException")
    void dejaValidee_leveException() {
        CommandeAchat commande = commande();
        commande.validerInterne();
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        var command = new ValiderCommandeAchatCommand(commande.getId().getValue());
        assertThatThrownBy(() -> sut.valider(command))
                .isInstanceOf(TransitionStatutCommandeAchatInvalideException.class);
    }
}
