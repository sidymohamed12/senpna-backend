package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.criteria.CommandeAchatSearchCriteria;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCommandeAchatUseCaseImpl — vue PNA, tous statuts confondus")
class ListCommandeAchatUseCaseImplTest {

        @Mock
        CommandeAchatRepositoryPort commandeAchatRepositoryPort;
        @Mock
        CommandeAchatDetailAssembler commandeAchatDetailAssembler;

        ListCommandeAchatUseCaseImpl sut;

        @BeforeEach
        void setUp() {
                sut = new ListCommandeAchatUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
        }

        @Test
        @DisplayName("transmet la recherche et le statut demandés au critère de recherche")
        void transmetCritereTelQuel() {
                LigneCommandeAchat ligne = LigneCommandeAchat.creer(MedicamentId.generate(),
                                ConditionnementId.generate(),
                                BigDecimal.TEN, BigDecimal.TEN);
                CommandeAchat commande = CommandeAchat.creer("BC-1", FournisseurId.generate(), EntrepotId.generate(),
                                List.of(ligne), null);
                when(commandeAchatRepositoryPort.search(any(), any()))
                                .thenReturn(PageResult.of(List.of(commande), 0, 20, 1));
                CommandeAchatSummary summary = new CommandeAchatSummary(commande.getId().getValue(), "BC-1",
                                commande.getFournisseurId().getValue(), StatutCommandeAchat.EN_ATTENTE_VALIDATION, 1,
                                commande.getCreatedAt());
                when(commandeAchatDetailAssembler.assemblerResume(commande)).thenReturn(summary);

                CommandeAchatPage result = sut.lister(
                                new ListCommandeAchatQuery("BC", StatutCommandeAchat.VALIDEE, 0, 20, "reference",
                                                "ASC"));

                ArgumentCaptor<CommandeAchatSearchCriteria> captor = ArgumentCaptor
                                .forClass(CommandeAchatSearchCriteria.class);
                verify(commandeAchatRepositoryPort).search(captor.capture(), any(PageRequest.class));
                assertThat(captor.getValue().recherche()).isEqualTo("BC");
                assertThat(captor.getValue().statut()).isEqualTo(StatutCommandeAchat.VALIDEE);
                assertThat(result.content()).containsExactly(summary);
        }
}
