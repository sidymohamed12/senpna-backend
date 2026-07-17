package ministere.sante.senpna.appeloffre.application.service;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OffreDetailAssembler")
class OffreDetailAssemblerTest {

    private final OffreDetailAssembler sut = new OffreDetailAssembler();

    @Test
    @DisplayName("assembler() mappe tous les champs, y compris les lignes de prix")
    void assembler_mappeTousLesChamps() {
        LigneOffre ligne = LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.valueOf(2000), 20);
        OffreFournisseur offre = OffreFournisseur.soumettre(new OffreFournisseur.SoumissionCommand(AppelOffreId.generate(), FournisseurId.generate(),
                "Commentaire", List.of(ligne)));

        OffreDetail detail = sut.assembler(offre);

        assertThat(detail.id()).isEqualTo(offre.getId().getValue());
        assertThat(detail.statut()).isEqualTo(StatutOffre.SOUMISE);
        assertThat(detail.commentaire()).isEqualTo("Commentaire");
        assertThat(detail.lignes()).hasSize(1);
        assertThat(detail.lignes().get(0).prixUnitaire()).isEqualByComparingTo("2000");
    }
}
