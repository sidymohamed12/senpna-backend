package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.InfoReceptionLigneInput;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ReceptionnerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.AvisExpedition;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceptionnerCommandeAchatUseCaseImpl")
class ReceptionnerCommandeAchatUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    ReceptionnerCommandeAchatUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ReceptionnerCommandeAchatUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
    }

    private CommandeAchat commandeExpediee() {
        LigneCommandeAchat ligne = LigneCommandeAchat.creer(new LigneCommandeAchat.CreationCommand(MedicamentId.generate(), ConditionnementId.generate(),
                BigDecimal.TEN, BigDecimal.TEN));
        CommandeAchat commande = CommandeAchat.creer(new CommandeAchat.CreationCommand("BC-1", FournisseurId.generate(), EntrepotId.generate(),
                List.of(ligne), null));
        commande.validerInterne();
        commande.confirmerDelaiLivraison(10, LocalDate.now().plusDays(10));
        commande.genererAvisExpedition(AvisExpedition.of(LocalDate.now(), null, null, null),
                List.of(new CommandeAchat.InfoExpeditionLigne(ligne.getId(), "LOT-1", null,
                        LocalDate.now().plusYears(1), null, BigDecimal.TEN)));
        return commande;
    }

    @Test
    @DisplayName("commande introuvable → CommandeAchatIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(commandeAchatRepositoryPort.findById(CommandeAchatId.of(id))).thenReturn(Optional.empty());

        var command = new ReceptionnerCommandeAchatCommand(id,
                List.of(new InfoReceptionLigneInput(UUID.randomUUID(), BigDecimal.TEN, BigDecimal.ZERO, null)));
        assertThatThrownBy(() -> sut.receptionner(command)).isInstanceOf(CommandeAchatIntrouvableException.class);
    }

    @Test
    @DisplayName("réception totale → RECEPTIONNEE")
    void receptionTotale_passeReceptionnee() {
        CommandeAchat commande = commandeExpediee();
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UUID ligneId = commande.getLignes().get(0).getId().getValue();
        sut.receptionner(new ReceptionnerCommandeAchatCommand(commande.getId().getValue(),
                List.of(new InfoReceptionLigneInput(ligneId, BigDecimal.TEN, BigDecimal.ZERO, null))));

        assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.RECEPTIONNEE);
    }

    @Test
    @DisplayName("réception partielle → PARTIELLEMENT_RECEPTIONNEE")
    void receptionPartielle_passePartiellementReceptionnee() {
        CommandeAchat commande = commandeExpediee();
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UUID ligneId = commande.getLignes().get(0).getId().getValue();
        sut.receptionner(new ReceptionnerCommandeAchatCommand(commande.getId().getValue(), List
                .of(new InfoReceptionLigneInput(ligneId, BigDecimal.valueOf(4), BigDecimal.ZERO, null))));

        assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.PARTIELLEMENT_RECEPTIONNEE);
    }
}
