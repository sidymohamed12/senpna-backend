package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.MarquerFacturePayeeCommand;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutFactureInvalideException;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarquerFacturePayeeUseCaseImpl")
class MarquerFacturePayeeUseCaseImplTest {

    @Mock
    FactureRepositoryPort factureRepositoryPort;
    @Mock
    FactureDetailAssembler factureDetailAssembler;

    MarquerFacturePayeeUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new MarquerFacturePayeeUseCaseImpl(factureRepositoryPort, factureDetailAssembler);
    }

    @Test
    @DisplayName("depuis VALIDEE → PAYEE")
    void depuisValidee_passePayee() {
        Facture facture = Facture.soumettre(CommandeAchatId.generate(), FournisseurId.generate(), "FAC-1",
                BigDecimal.TEN, LocalDate.now(), null, null);
        facture.valider();
        when(factureRepositoryPort.findById(facture.getId())).thenReturn(Optional.of(facture));
        when(factureRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.marquerPayee(new MarquerFacturePayeeCommand(facture.getId().getValue()));

        assertThat(facture.getStatut()).isEqualTo(StatutFacture.PAYEE);
    }

    @Test
    @DisplayName("depuis SOUMISE (non validée) → TransitionStatutFactureInvalideException")
    void depuisSoumise_leveException() {
        Facture facture = Facture.soumettre(CommandeAchatId.generate(), FournisseurId.generate(), "FAC-1",
                BigDecimal.TEN, LocalDate.now(), null, null);
        when(factureRepositoryPort.findById(facture.getId())).thenReturn(Optional.of(facture));

        var command = new MarquerFacturePayeeCommand(facture.getId().getValue());
        assertThatThrownBy(() -> sut.marquerPayee(command))
                .isInstanceOf(TransitionStatutFactureInvalideException.class);
    }
}
