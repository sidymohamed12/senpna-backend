package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListMesCommandesQuery;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListMesCommandesUseCaseImpl — commandes du fournisseur connecté")
class ListMesCommandesUseCaseImplTest {

        @Mock
        CommandeAchatRepositoryPort commandeAchatRepositoryPort;
        @Mock
        CommandeAchatDetailAssembler commandeAchatDetailAssembler;

        ListMesCommandesUseCaseImpl sut;

        @BeforeEach
        void setUp() {
                sut = new ListMesCommandesUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
        }

        @Test
        @DisplayName("transmet le fournisseur et le statut au port")
        void transmetFournisseurEtStatut() {
                FournisseurId fournisseurId = FournisseurId.generate();
                LigneCommandeAchat ligne = LigneCommandeAchat.creer(MedicamentId.generate(),
                                ConditionnementId.generate(),
                                BigDecimal.TEN, BigDecimal.TEN);
                CommandeAchat commande = CommandeAchat.creer("BC-1", fournisseurId, EntrepotId.generate(),
                                List.of(ligne),
                                null);
                when(commandeAchatRepositoryPort.findByFournisseurId(any(), any(), any()))
                                .thenReturn(PageResult.of(List.of(commande), 0, 20, 1));
                ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary summary = new ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary(
                                commande.getId().getValue(), "BC-1", fournisseurId.getValue(),
                                StatutCommandeAchat.EN_ATTENTE_VALIDATION, 1, commande.getCreatedAt());
                when(commandeAchatDetailAssembler.assemblerResume(commande)).thenReturn(summary);

                CommandeAchatPage result = sut.lister(
                                new ListMesCommandesQuery(fournisseurId.getValue(),
                                                StatutCommandeAchat.EN_ATTENTE_VALIDATION, 0, 20));

                ArgumentCaptor<FournisseurId> captor = ArgumentCaptor.forClass(FournisseurId.class);
                verify(commandeAchatRepositoryPort).findByFournisseurId(captor.capture(),
                                eq(StatutCommandeAchat.EN_ATTENTE_VALIDATION), any());
                assertThat(captor.getValue()).isEqualTo(fournisseurId);
                assertThat(result.content()).containsExactly(summary);
        }
}
