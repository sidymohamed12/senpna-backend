package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.SoumettreFactureCommand;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoumettreFactureUseCaseImpl")
class SoumettreFactureUseCaseImplTest {

    @Mock
    FactureRepositoryPort factureRepositoryPort;
    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    FactureDetailAssembler factureDetailAssembler;

    SoumettreFactureUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new SoumettreFactureUseCaseImpl(factureRepositoryPort, commandeAchatRepositoryPort,
                factureDetailAssembler);
    }

    private CommandeAchat commande(FournisseurId fournisseurId) {
        LigneCommandeAchat ligne = LigneCommandeAchat.creer(MedicamentId.generate(), ConditionnementId.generate(),
                BigDecimal.TEN, BigDecimal.TEN);
        return CommandeAchat.creer("BC-1", fournisseurId, EntrepotId.generate(), List.of(ligne), null);
    }

    private SoumettreFactureCommand commandeFacture(UUID commandeAchatId, UUID fournisseurId) {
        return new SoumettreFactureCommand(commandeAchatId, fournisseurId, "FAC-1",
                BigDecimal.valueOf(3_500_000), LocalDate.now(), LocalDate.now().plusDays(30), "media-1");
    }

    @Test
    @DisplayName("commande introuvable → CommandeAchatIntrouvableException")
    void commandeIntrouvable_leveException() {
        UUID commandeId = UUID.randomUUID();
        when(commandeAchatRepositoryPort.findById(CommandeAchatId.of(commandeId))).thenReturn(Optional.empty());

        var command = commandeFacture(commandeId, UUID.randomUUID());
        assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(CommandeAchatIntrouvableException.class);

        verify(factureRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("commande destinée à un autre fournisseur → AccesCommandeAchatRefuseException")
    void autreFournisseur_leveException() {
        CommandeAchat commande = commande(FournisseurId.generate());
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));

        var command = commandeFacture(commande.getId().getValue(), UUID.randomUUID());
        assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(AccesCommandeAchatRefuseException.class);
    }

    @Test
    @DisplayName("fournisseur propriétaire → facture soumise")
    void proprietaire_soumetFacture() {
        FournisseurId fournisseurId = FournisseurId.generate();
        CommandeAchat commande = commande(fournisseurId);
        when(commandeAchatRepositoryPort.findById(commande.getId())).thenReturn(Optional.of(commande));
        when(factureRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        FactureDetail detail = new FactureDetail(UUID.randomUUID(), commande.getId().getValue(),
                fournisseurId.getValue(), "FAC-1", BigDecimal.TEN, LocalDate.now(), null, null, null, null, null,
                null);
        when(factureDetailAssembler.assembler(any(Facture.class))).thenReturn(detail);

        FactureDetail result = sut.soumettre(commandeFacture(commande.getId().getValue(), fournisseurId.getValue()));

        assertThat(result).isSameAs(detail);
        verify(factureRepositoryPort).save(any());
    }
}
