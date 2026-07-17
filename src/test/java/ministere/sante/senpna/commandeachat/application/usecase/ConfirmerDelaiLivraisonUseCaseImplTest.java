package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ConfirmerDelaiLivraisonCommand;
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
@DisplayName("ConfirmerDelaiLivraisonUseCaseImpl")
class ConfirmerDelaiLivraisonUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    ConfirmerDelaiLivraisonUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ConfirmerDelaiLivraisonUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
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
    @DisplayName("commande destinée à un autre fournisseur → AccesCommandeAchatRefuseException")
    void autreFournisseur_leveException() {
        CommandeAchat commande = commandeValidee(FournisseurId.generate());
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        var command = new ConfirmerDelaiLivraisonCommand(commande.getId().getValue(), UUID.randomUUID(), 10,
                LocalDate.now().plusDays(10));
        assertThatThrownBy(() -> sut.confirmer(command)).isInstanceOf(AccesCommandeAchatRefuseException.class);
    }

    @Test
    @DisplayName("fournisseur propriétaire → EN_TRANSIT")
    void proprietaire_confirmeDelai() {
        FournisseurId fournisseurId = FournisseurId.generate();
        CommandeAchat commande = commandeValidee(fournisseurId);
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.confirmer(new ConfirmerDelaiLivraisonCommand(commande.getId().getValue(), fournisseurId.getValue(), 12,
                LocalDate.now().plusDays(12)));

        assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.EN_TRANSIT);
        assertThat(commande.getDelaiLivraisonConfirmeJours()).isEqualTo(12);
    }
}
