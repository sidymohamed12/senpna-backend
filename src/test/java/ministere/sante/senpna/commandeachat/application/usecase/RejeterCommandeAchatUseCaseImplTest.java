package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.RejeterCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejeterCommandeAchatUseCaseImpl")
class RejeterCommandeAchatUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    RejeterCommandeAchatUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new RejeterCommandeAchatUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
    }

    @Test
    @DisplayName("cas nominal → REJETEE avec motif enregistré")
    void casNominal_passeRejetee() {
        LigneCommandeAchat ligne = LigneCommandeAchat.creer(MedicamentId.generate(), ConditionnementId.generate(),
                BigDecimal.TEN, BigDecimal.TEN);
        CommandeAchat commande = CommandeAchat.creer("BC-1", FournisseurId.generate(), EntrepotId.generate(),
                List.of(ligne), null);
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.rejeter(new RejeterCommandeAchatCommand(commande.getId().getValue(), "Prix hors marché"));

        assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.REJETEE);
        assertThat(commande.getMotifRejet()).isEqualTo("Prix hors marché");
    }
}
