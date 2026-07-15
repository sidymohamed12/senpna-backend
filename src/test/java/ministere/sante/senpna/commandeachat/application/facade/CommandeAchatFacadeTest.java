package ministere.sante.senpna.commandeachat.application.facade;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AnnulerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CreateCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ReceptionnerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.RejeterCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ValiderCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.MarquerFacturePayeeCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.RejeterFactureCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ValiderFactureCommand;
import ministere.sante.senpna.commandeachat.domain.port.in.AnnulerCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.CreateCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GetCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListFacturesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.MarquerFacturePayeeUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ReceptionnerCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.RejeterCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.RejeterFactureUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ValiderCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ValiderFactureUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommandeAchatFacade — délégation aux use cases (PNA)")
class CommandeAchatFacadeTest {

        @Mock
        CreateCommandeAchatUseCase createCommandeAchatUseCase;
        @Mock
        ValiderCommandeAchatUseCase validerCommandeAchatUseCase;
        @Mock
        RejeterCommandeAchatUseCase rejeterCommandeAchatUseCase;
        @Mock
        AnnulerCommandeAchatUseCase annulerCommandeAchatUseCase;
        @Mock
        ReceptionnerCommandeAchatUseCase receptionnerCommandeAchatUseCase;
        @Mock
        GetCommandeAchatUseCase getCommandeAchatUseCase;
        @Mock
        ListCommandeAchatUseCase listCommandeAchatUseCase;
        @Mock
        ValiderFactureUseCase validerFactureUseCase;
        @Mock
        RejeterFactureUseCase rejeterFactureUseCase;
        @Mock
        MarquerFacturePayeeUseCase marquerFacturePayeeUseCase;
        @Mock
        ListFacturesUseCase listFacturesUseCase;

        CommandeAchatFacade sut;

        @BeforeEach
        void setUp() {
                sut = new CommandeAchatFacade(createCommandeAchatUseCase, validerCommandeAchatUseCase,
                                rejeterCommandeAchatUseCase, annulerCommandeAchatUseCase,
                                receptionnerCommandeAchatUseCase,
                                getCommandeAchatUseCase, listCommandeAchatUseCase, validerFactureUseCase,
                                rejeterFactureUseCase,
                                marquerFacturePayeeUseCase, listFacturesUseCase);
        }

        @Test
        @DisplayName("chaque méthode de façade délègue au use case correspondant")
        void chaqueMethodeDelegue() {
                UUID id = UUID.randomUUID();
                CommandeAchatDetail commandeDetail = new CommandeAchatDetail(id, "BC-1", id, id, null, List.of(), null,
                                null, null, null, null, null, null, null);
                CommandeAchatPage commandePage = new CommandeAchatPage(List.of(), 0, 20, 0, 0);
                FactureDetail factureDetail = new FactureDetail(id, id, id, "FAC-1", null, null, null, null, null, null,
                                null, null);
                FacturePage facturePage = new FacturePage(List.of(), 0, 20, 0, 0);

                when(createCommandeAchatUseCase.creer(any())).thenReturn(commandeDetail);
                when(validerCommandeAchatUseCase.valider(any())).thenReturn(commandeDetail);
                when(rejeterCommandeAchatUseCase.rejeter(any())).thenReturn(commandeDetail);
                when(annulerCommandeAchatUseCase.annuler(any())).thenReturn(commandeDetail);
                when(receptionnerCommandeAchatUseCase.receptionner(any())).thenReturn(commandeDetail);
                when(getCommandeAchatUseCase.obtenir(any())).thenReturn(commandeDetail);
                when(listCommandeAchatUseCase.lister(any())).thenReturn(commandePage);
                when(validerFactureUseCase.valider(any())).thenReturn(factureDetail);
                when(rejeterFactureUseCase.rejeter(any())).thenReturn(factureDetail);
                when(marquerFacturePayeeUseCase.marquerPayee(any())).thenReturn(factureDetail);
                when(listFacturesUseCase.lister(any())).thenReturn(facturePage);

                assertThat(sut.creer(new CreateCommandeAchatCommand("BC-1", id, id, null, List.of())))
                                .isSameAs(commandeDetail);
                assertThat(sut.valider(new ValiderCommandeAchatCommand(id))).isSameAs(commandeDetail);
                assertThat(sut.rejeter(new RejeterCommandeAchatCommand(id, null))).isSameAs(commandeDetail);
                assertThat(sut.annuler(new AnnulerCommandeAchatCommand(id))).isSameAs(commandeDetail);
                assertThat(sut.receptionner(new ReceptionnerCommandeAchatCommand(id, List.of())))
                                .isSameAs(commandeDetail);
                assertThat(sut.obtenir(new GetCommandeAchatQuery(id, null))).isSameAs(commandeDetail);
                assertThat(sut.lister(new ListCommandeAchatQuery(null, null, 0, 20, null, null)))
                                .isSameAs(commandePage);
                assertThat(sut.validerFacture(new ValiderFactureCommand(id))).isSameAs(factureDetail);
                assertThat(sut.rejeterFacture(new RejeterFactureCommand(id, null))).isSameAs(factureDetail);
                assertThat(sut.marquerFacturePayee(new MarquerFacturePayeeCommand(id))).isSameAs(factureDetail);
                assertThat(sut.listerFactures(new ListFacturesQuery(null, 0, 20))).isSameAs(facturePage);
        }
}
