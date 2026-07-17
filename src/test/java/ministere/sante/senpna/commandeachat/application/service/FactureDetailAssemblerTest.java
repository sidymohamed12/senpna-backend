package ministere.sante.senpna.commandeachat.application.service;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FactureDetailAssembler")
class FactureDetailAssemblerTest {

    private final FactureDetailAssembler sut = new FactureDetailAssembler();

    @Test
    @DisplayName("assembler() mappe tous les champs")
    void assembler_mappeTousLesChamps() {
        Facture facture = Facture.soumettre(new Facture.SoumissionCommand(CommandeAchatId.generate(), FournisseurId.generate(), "FAC-1",
                BigDecimal.valueOf(3_500_000), LocalDate.now(), LocalDate.now().plusDays(30), "media-1"));

        FactureDetail detail = sut.assembler(facture);

        assertThat(detail.numeroFacture()).isEqualTo("FAC-1");
        assertThat(detail.statut()).isEqualTo(StatutFacture.SOUMISE);
        assertThat(detail.montant()).isEqualByComparingTo("3500000");
        assertThat(detail.pieceJointeMediaId()).isEqualTo("media-1");
    }
}
