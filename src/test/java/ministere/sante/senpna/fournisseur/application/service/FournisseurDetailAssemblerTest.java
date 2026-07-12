package ministere.sante.senpna.fournisseur.application.service;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FournisseurDetailAssembler — assemblage de la représentation FournisseurDetail")
class FournisseurDetailAssemblerTest {

    FournisseurDetailAssembler sut = new FournisseurDetailAssembler();

    @Test
    @DisplayName("reporte fidèlement chaque champ de l'agrégat")
    void reporteChaqueChamp() {
        Fournisseur fournisseur = Fournisseur.creer("Pharma Plus", "Dakar", "+221771234567",
                "contact@pharmaplus.sn", "Awa Fall");

        FournisseurDetail detail = sut.assembler(fournisseur);

        assertThat(detail.id()).isEqualTo(fournisseur.getId().getValue());
        assertThat(detail.nom()).isEqualTo("Pharma Plus");
        assertThat(detail.adresse()).isEqualTo("Dakar");
        assertThat(detail.telephone()).isEqualTo("+221771234567");
        assertThat(detail.email()).isEqualTo("contact@pharmaplus.sn");
        assertThat(detail.contactPrincipal()).isEqualTo("Awa Fall");
        assertThat(detail.actif()).isTrue();
        assertThat(detail.createdAt()).isEqualTo(fournisseur.getCreatedAt());
        assertThat(detail.updatedAt()).isEqualTo(fournisseur.getUpdatedAt());
    }
}
