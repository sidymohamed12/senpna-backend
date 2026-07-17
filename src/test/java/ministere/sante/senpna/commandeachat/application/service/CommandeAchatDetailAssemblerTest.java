package ministere.sante.senpna.commandeachat.application.service;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary;
import ministere.sante.senpna.commandeachat.domain.model.AvisExpedition;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommandeAchatDetailAssembler")
class CommandeAchatDetailAssemblerTest {

    private final CommandeAchatDetailAssembler sut = new CommandeAchatDetailAssembler();

    private LigneCommandeAchat ligne() {
        return LigneCommandeAchat.creer(new LigneCommandeAchat.CreationCommand(MedicamentId.generate(), ConditionnementId.generate(), BigDecimal.TEN,
                BigDecimal.valueOf(200_000)));
    }

    @Test
    @DisplayName("assembler() mappe tous les champs sans avis d'expédition")
    void assembler_sansAvisExpedition() {
        CommandeAchat commande = CommandeAchat.creer(new CommandeAchat.CreationCommand("BC-1", FournisseurId.generate(), EntrepotId.generate(),
                List.of(ligne()), "Com"));

        CommandeAchatDetail detail = sut.assembler(commande);

        assertThat(detail.reference()).isEqualTo("BC-1");
        assertThat(detail.statut()).isEqualTo(StatutCommandeAchat.EN_ATTENTE_VALIDATION);
        assertThat(detail.avisExpedition()).isNull();
        assertThat(detail.lignes()).hasSize(1);
    }

    @Test
    @DisplayName("assembler() mappe l'avis d'expédition lorsqu'il est présent")
    void assembler_avecAvisExpedition() {
        CommandeAchat commande = CommandeAchat.creer(new CommandeAchat.CreationCommand("BC-1", FournisseurId.generate(), EntrepotId.generate(),
                List.of(ligne()), null));
        commande.validerInterne();
        commande.confirmerDelaiLivraison(10, LocalDate.now().plusDays(10));
        commande.genererAvisExpedition(AvisExpedition.of(LocalDate.now(), "DHL", "T-1", null),
                List.of(new CommandeAchat.InfoExpeditionLigne(commande.getLignes().get(0).getId(), "LOT-1", null,
                        LocalDate.now().plusYears(1), null, BigDecimal.TEN)));

        CommandeAchatDetail detail = sut.assembler(commande);

        assertThat(detail.avisExpedition()).isNotNull();
        assertThat(detail.avisExpedition().transporteur()).isEqualTo("DHL");
        assertThat(detail.lignes().get(0).numeroLot()).isEqualTo("LOT-1");
    }

    @Test
    @DisplayName("assemblerResume() mappe le nombre de lignes")
    void assemblerResume_mappeNombreLignes() {
        CommandeAchat commande = CommandeAchat.creer(new CommandeAchat.CreationCommand("BC-1", FournisseurId.generate(), EntrepotId.generate(),
                List.of(ligne()), null));

        CommandeAchatSummary summary = sut.assemblerResume(commande);

        assertThat(summary.nombreLignes()).isEqualTo(1);
        assertThat(summary.reference()).isEqualTo("BC-1");
    }
}
