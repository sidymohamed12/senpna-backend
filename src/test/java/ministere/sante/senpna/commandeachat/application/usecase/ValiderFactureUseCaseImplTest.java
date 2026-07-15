package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ValiderFactureCommand;
import ministere.sante.senpna.commandeachat.domain.exception.FactureIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValiderFactureUseCaseImpl")
class ValiderFactureUseCaseImplTest {

    @Mock
    FactureRepositoryPort factureRepositoryPort;
    @Mock
    FactureDetailAssembler factureDetailAssembler;

    ValiderFactureUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ValiderFactureUseCaseImpl(factureRepositoryPort, factureDetailAssembler);
    }

    @Test
    @DisplayName("facture introuvable → FactureIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(factureRepositoryPort.findById(FactureId.of(id))).thenReturn(Optional.empty());

        var command = new ValiderFactureCommand(id);
        assertThatThrownBy(() -> sut.valider(command)).isInstanceOf(FactureIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal → VALIDEE")
    void casNominal_passeValidee() {
        Facture facture = Facture.soumettre(CommandeAchatId.generate(), FournisseurId.generate(), "FAC-1",
                BigDecimal.TEN, LocalDate.now(), null, null);
        when(factureRepositoryPort.findById(facture.getId())).thenReturn(Optional.of(facture));
        when(factureRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.valider(new ValiderFactureCommand(facture.getId().getValue()));

        assertThat(facture.getStatut()).isEqualTo(StatutFacture.VALIDEE);
    }
}
