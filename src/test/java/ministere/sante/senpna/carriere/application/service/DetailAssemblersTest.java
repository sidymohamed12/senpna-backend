package ministere.sante.senpna.carriere.application.service;

import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.Phone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Assembleurs de détail du module carriere")
class DetailAssemblersTest {

    @Test
    @DisplayName("CandidatureDetailAssembler reporte fidèlement chaque champ")
    void candidatureDetailAssembler_reporteChaqueChamp() {
        UUID opportuniteId = UUID.randomUUID();
        Candidature candidature = Candidature.soumettre(opportuniteId, Civilite.MME, "Awa Fall",
                Email.of("awa.fall@mail.sn"), Phone.of("+221771234567"), "https://cv.pdf", null, "Motive",
                true, "Developpeur", "Entreprise X", "rh@entreprise.sn");

        CandidatureDetail detail = new CandidatureDetailAssembler().assembler(candidature);

        assertThat(detail.opportuniteId()).isEqualTo(opportuniteId);
        assertThat(detail.civilite()).isEqualTo("MME");
        assertThat(detail.nomComplet()).isEqualTo("Awa Fall");
        assertThat(detail.email()).isEqualTo("awa.fall@mail.sn");
        assertThat(detail.cvUrl()).isEqualTo("https://cv.pdf");
        assertThat(detail.consentementRgpd()).isTrue();
    }

    @Test
    @DisplayName("OpportuniteCarriereDetailAssembler utilise le statut EFFECTIF, pas le statut persisté")
    void opportuniteDetailAssembler_utiliseStatutEffectif() {
        OpportuniteCarriere opportunite = OpportuniteCarriere.creer("Developpeur", "Entreprise X", "Description",
                null, "Dakar", TypeContrat.CDI, LocalDate.now(), LocalDate.now().minusDays(1), UUID.randomUUID(),
                "Auteur", "rh@entreprise.sn");
        // date limite deja depassee des la creation (brouillon) : le statut
        // effectif reste BROUILLON tant que le statut persiste n'est pas OUVERT/EN_COURS

        OpportuniteCarriereDetail detail = new OpportuniteCarriereDetailAssembler().assembler(opportunite);

        assertThat(detail.statut()).isEqualTo("BROUILLON");
        assertThat(detail.titre()).isEqualTo("Developpeur");
        assertThat(detail.typeContrat()).isEqualTo("CDI");
    }
}
