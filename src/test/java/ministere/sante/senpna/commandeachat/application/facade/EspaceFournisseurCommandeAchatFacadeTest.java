package ministere.sante.senpna.commandeachat.application.facade;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AccuserReceptionCommandeCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ConfirmerDelaiLivraisonCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GenererAvisExpeditionCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListMesCommandesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.GetFactureQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListMesFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.SoumettreFactureCommand;
import ministere.sante.senpna.commandeachat.domain.port.in.AccuserReceptionCommandeUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ConfirmerDelaiLivraisonUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GenererAvisExpeditionUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GetCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GetFactureUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListMesCommandesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListMesFacturesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.SoumettreFactureUseCase;

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
@DisplayName("EspaceFournisseurCommandeAchatFacade — délégation aux use cases (espace fournisseur)")
class EspaceFournisseurCommandeAchatFacadeTest {

        @Mock
        AccuserReceptionCommandeUseCase accuserReceptionCommandeUseCase;
        @Mock
        ConfirmerDelaiLivraisonUseCase confirmerDelaiLivraisonUseCase;
        @Mock
        GenererAvisExpeditionUseCase genererAvisExpeditionUseCase;
        @Mock
        GetCommandeAchatUseCase getCommandeAchatUseCase;
        @Mock
        ListMesCommandesUseCase listMesCommandesUseCase;
        @Mock
        SoumettreFactureUseCase soumettreFactureUseCase;
        @Mock
        GetFactureUseCase getFactureUseCase;
        @Mock
        ListMesFacturesUseCase listMesFacturesUseCase;

        EspaceFournisseurCommandeAchatFacade sut;

        @BeforeEach
        void setUp() {
                sut = new EspaceFournisseurCommandeAchatFacade(accuserReceptionCommandeUseCase,
                                confirmerDelaiLivraisonUseCase, genererAvisExpeditionUseCase, getCommandeAchatUseCase,
                                listMesCommandesUseCase, soumettreFactureUseCase, getFactureUseCase,
                                listMesFacturesUseCase);
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

                when(accuserReceptionCommandeUseCase.accuserReception(any())).thenReturn(commandeDetail);
                when(confirmerDelaiLivraisonUseCase.confirmer(any())).thenReturn(commandeDetail);
                when(genererAvisExpeditionUseCase.generer(any())).thenReturn(commandeDetail);
                when(getCommandeAchatUseCase.obtenir(any())).thenReturn(commandeDetail);
                when(listMesCommandesUseCase.lister(any())).thenReturn(commandePage);
                when(soumettreFactureUseCase.soumettre(any())).thenReturn(factureDetail);
                when(getFactureUseCase.obtenir(any())).thenReturn(factureDetail);
                when(listMesFacturesUseCase.lister(any())).thenReturn(facturePage);

                assertThat(sut.accuserReception(new AccuserReceptionCommandeCommand(id, id))).isSameAs(commandeDetail);
                assertThat(sut.confirmerDelaiLivraison(new ConfirmerDelaiLivraisonCommand(id, id, 10, null)))
                                .isSameAs(commandeDetail);
                assertThat(sut.genererAvisExpedition(new GenererAvisExpeditionCommand(id, id, null, null, null, null,
                                List.of()))).isSameAs(commandeDetail);
                assertThat(sut.obtenirCommande(new GetCommandeAchatQuery(id, id))).isSameAs(commandeDetail);
                assertThat(sut.listerMesCommandes(new ListMesCommandesQuery(id, null, 0, 20))).isSameAs(commandePage);
                assertThat(sut.soumettreFacture(new SoumettreFactureCommand(id, id, "FAC-1", null, null, null, null)))
                                .isSameAs(factureDetail);
                assertThat(sut.obtenirFacture(new GetFactureQuery(id, id))).isSameAs(factureDetail);
                assertThat(sut.listerMesFactures(new ListMesFacturesQuery(id, null, 0, 20))).isSameAs(facturePage);
        }
}
