package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.RejeterFactureCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejeterFactureUseCaseImpl")
class RejeterFactureUseCaseImplTest {

    @Mock
    FactureRepositoryPort factureRepositoryPort;
    @Mock
    FactureDetailAssembler factureDetailAssembler;

    RejeterFactureUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new RejeterFactureUseCaseImpl(factureRepositoryPort, factureDetailAssembler);
    }

    @Test
    @DisplayName("cas nominal → REJETEE avec motif enregistré")
    void casNominal_passeRejetee() {
        Facture facture = Facture.soumettre(new Facture.SoumissionCommand(CommandeAchatId.generate(), FournisseurId.generate(), "FAC-1",
                BigDecimal.TEN, LocalDate.now(), null, null));
        when(factureRepositoryPort.findById(facture.getId())).thenReturn(Optional.of(facture));
        when(factureRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.rejeter(new RejeterFactureCommand(facture.getId().getValue(), "Montant incohérent"));

        assertThat(facture.getStatut()).isEqualTo(StatutFacture.REJETEE);
        assertThat(facture.getMotifRejet()).isEqualTo("Montant incohérent");
    }
}
