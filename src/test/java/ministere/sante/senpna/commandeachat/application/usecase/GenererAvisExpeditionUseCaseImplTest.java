package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GenererAvisExpeditionCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.InfoExpeditionLigneInput;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenererAvisExpeditionUseCaseImpl")
class GenererAvisExpeditionUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    GenererAvisExpeditionUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GenererAvisExpeditionUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
    }

    private CommandeAchat commandeEnTransit(FournisseurId fournisseurId) {
        LigneCommandeAchat ligne = LigneCommandeAchat.creer(MedicamentId.generate(), ConditionnementId.generate(),
                BigDecimal.TEN, BigDecimal.TEN);
        CommandeAchat commande = CommandeAchat.creer("BC-1", fournisseurId, EntrepotId.generate(), List.of(ligne),
                null);
        commande.validerInterne();
        commande.confirmerDelaiLivraison(10, LocalDate.now().plusDays(10));
        return commande;
    }

    @Test
    @DisplayName("commande destinée à un autre fournisseur → AccesCommandeAchatRefuseException")
    void autreFournisseur_leveException() {
        CommandeAchat commande = commandeEnTransit(FournisseurId.generate());
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        UUID ligneId = commande.getLignes().get(0).getId().getValue();
        var command = new GenererAvisExpeditionCommand(commande.getId().getValue(), UUID.randomUUID(),
                LocalDate.now(), "DHL", "T-1", null,
                List.of(new InfoExpeditionLigneInput(ligneId, "LOT-1", null, LocalDate.now().plusYears(1), null,
                        BigDecimal.TEN)));

        assertThatThrownBy(() -> sut.generer(command)).isInstanceOf(AccesCommandeAchatRefuseException.class);
    }

    @Test
    @DisplayName("fournisseur propriétaire → EXPEDIEE, lot renseigné")
    void proprietaire_genereAvis() {
        FournisseurId fournisseurId = FournisseurId.generate();
        CommandeAchat commande = commandeEnTransit(fournisseurId);
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UUID ligneId = commande.getLignes().get(0).getId().getValue();
        sut.generer(new GenererAvisExpeditionCommand(commande.getId().getValue(), fournisseurId.getValue(),
                LocalDate.now(), "DHL", "T-1", LocalDate.now().plusDays(3),
                List.of(new InfoExpeditionLigneInput(ligneId, "LOT-A001", null, LocalDate.now().plusYears(1), null,
                        BigDecimal.TEN))));

        assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.EXPEDIEE);
        assertThat(commande.getLignes().get(0).getNumeroLot()).isEqualTo("LOT-A001");
        assertThat(commande.getAvisExpedition().getTransporteur()).isEqualTo("DHL");
    }
}
