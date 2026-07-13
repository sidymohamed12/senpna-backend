package ministere.sante.senpna.projet.application.service;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjetDetailAssembler — assemblage de la représentation ProjetDetail")
class ProjetDetailAssemblerTest {

    ProjetDetailAssembler sut = new ProjetDetailAssembler();

    @Test
    @DisplayName("reporte fidèlement chaque champ, catégorie et statut en String")
    void reporteChaqueChamp() {
        Projet projet = Projet.creer(CategorieProjet.SANTE, "Vaccination rurale", "Description",
                List.of("Objectif 1"), List.of("Impact 1"), "https://img");

        ProjetDetail detail = sut.assembler(projet);

        assertThat(detail.id()).isEqualTo(projet.getId().getValue());
        assertThat(detail.categorie()).isEqualTo("SANTE");
        assertThat(detail.nom()).isEqualTo("Vaccination rurale");
        assertThat(detail.objectifs()).containsExactly("Objectif 1");
        assertThat(detail.impacts()).containsExactly("Impact 1");
        assertThat(detail.statut()).isEqualTo("BROUILLON");
    }
}
