package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AccuserReceptionCommandeCommand;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
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
@DisplayName("AccuserReceptionCommandeUseCaseImpl")
class AccuserReceptionCommandeUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    AccuserReceptionCommandeUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new AccuserReceptionCommandeUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
    }

    private CommandeAchat commandeValidee(FournisseurId fournisseurId) {
        LigneCommandeAchat ligne = LigneCommandeAchat.creer(new LigneCommandeAchat.CreationCommand(MedicamentId.generate(), ConditionnementId.generate(),
                BigDecimal.TEN, BigDecimal.TEN));
        CommandeAchat commande = CommandeAchat.creer(new CommandeAchat.CreationCommand("BC-1", fournisseurId, EntrepotId.generate(), List.of(ligne),
                null));
        commande.validerInterne();
        return commande;
    }

    @Test
    @DisplayName("commande introuvable → CommandeAchatIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(commandeAchatRepositoryPort.findById(CommandeAchatId.of(id))).thenReturn(Optional.empty());

        var command = new AccuserReceptionCommandeCommand(id, UUID.randomUUID());
        assertThatThrownBy(() -> sut.accuserReception(command))
                .isInstanceOf(CommandeAchatIntrouvableException.class);
    }

    @Test
    @DisplayName("commande destinée à un autre fournisseur → AccesCommandeAchatRefuseException")
    void autreFournisseur_leveException() {
        CommandeAchat commande = commandeValidee(FournisseurId.generate());
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        var command = new AccuserReceptionCommandeCommand(commande.getId().getValue(), UUID.randomUUID());
        assertThatThrownBy(() -> sut.accuserReception(command))
                .isInstanceOf(AccesCommandeAchatRefuseException.class);
    }

    @Test
    @DisplayName("fournisseur propriétaire → accusé de réception horodaté et sauvegardé")
    void proprietaire_accuseReception() {
        FournisseurId fournisseurId = FournisseurId.generate();
        CommandeAchat commande = commandeValidee(fournisseurId);
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.accuserReception(new AccuserReceptionCommandeCommand(commande.getId().getValue(),
                fournisseurId.getValue()));

        assertThat(commande.getDateAccuseReceptionFournisseur()).isNotNull();
    }
}
