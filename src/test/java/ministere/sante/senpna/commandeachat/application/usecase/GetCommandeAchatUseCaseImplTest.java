package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCommandeAchatUseCaseImpl")
class GetCommandeAchatUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    GetCommandeAchatUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GetCommandeAchatUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
    }

    private CommandeAchat commande(FournisseurId fournisseurId) {
        LigneCommandeAchat ligne = LigneCommandeAchat.creer(MedicamentId.generate(), ConditionnementId.generate(),
                BigDecimal.TEN, BigDecimal.TEN);
        return CommandeAchat.creer("BC-1", fournisseurId, EntrepotId.generate(), List.of(ligne), null);
    }

    @Test
    @DisplayName("fournisseurId null (appel PNA) → aucune restriction")
    void fournisseurIdNull_aucuneRestriction() {
        CommandeAchat commande = commande(FournisseurId.generate());
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        sut.obtenir(new GetCommandeAchatQuery(commande.getId().getValue(), null));

        verify(commandeAchatDetailAssembler).assembler(commande);
    }

    @Test
    @DisplayName("fournisseurId renseigné et différent → AccesCommandeAchatRefuseException")
    void fournisseurDifferent_leveException() {
        CommandeAchat commande = commande(FournisseurId.generate());
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        var query = new GetCommandeAchatQuery(commande.getId().getValue(), UUID.randomUUID());
        assertThatThrownBy(() -> sut.obtenir(query)).isInstanceOf(AccesCommandeAchatRefuseException.class);
    }

    @Test
    @DisplayName("fournisseurId renseigné et correspondant → autorisé")
    void fournisseurCorrespondant_autorise() {
        FournisseurId fournisseurId = FournisseurId.generate();
        CommandeAchat commande = commande(fournisseurId);
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        sut.obtenir(new GetCommandeAchatQuery(commande.getId().getValue(), fournisseurId.getValue()));

        verify(commandeAchatDetailAssembler).assembler(commande);
    }
}
