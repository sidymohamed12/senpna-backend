package ministere.sante.senpna.appeloffre.application.service;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreSummary;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppelOffreDetailAssembler")
class AppelOffreDetailAssemblerTest {

    private final AppelOffreDetailAssembler sut = new AppelOffreDetailAssembler();

    private AppelOffre appelOffre() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Amoxicilline", BigDecimal.TEN,
                "Comprimé");
        return AppelOffre.creer(new AppelOffre.CreationCommand("AO-2026-0001", "Objet", LocalDate.now().plusDays(10), List.of(ligne)));
    }

    @Test
    @DisplayName("assembler() mappe tous les champs, y compris les lignes")
    void assembler_mappeTousLesChamps() {
        AppelOffre appelOffre = appelOffre();

        AppelOffreDetail detail = sut.assembler(appelOffre);

        assertThat(detail.id()).isEqualTo(appelOffre.getId().getValue());
        assertThat(detail.reference()).isEqualTo("AO-2026-0001");
        assertThat(detail.statut()).isEqualTo(StatutAppelOffre.BROUILLON);
        assertThat(detail.lignes()).hasSize(1);
        assertThat(detail.lignes().get(0).designation()).isEqualTo("Amoxicilline");
    }

    @Test
    @DisplayName("assemblerResume() mappe le nombre de lignes sans le détail")
    void assemblerResume_mappeNombreLignes() {
        AppelOffre appelOffre = appelOffre();

        AppelOffreSummary summary = sut.assemblerResume(appelOffre);

        assertThat(summary.nombreLignes()).isEqualTo(1);
        assertThat(summary.reference()).isEqualTo("AO-2026-0001");
    }
}
