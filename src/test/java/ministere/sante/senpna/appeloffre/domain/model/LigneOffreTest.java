package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LigneOffre — entité de domaine")
class LigneOffreTest {

    @Test
    @DisplayName("crée une ligne d'offre valide")
    void creer_succes() {
        LigneOffre ligne = LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.valueOf(2000), 15);

        assertThat(ligne.getPrixUnitaire()).isEqualByComparingTo("2000");
        assertThat(ligne.getDelaiLivraisonJours()).isEqualTo(15);
    }

    @Test
    @DisplayName("prix unitaire nul ou négatif → IllegalArgumentException")
    void prixInvalide_leveException() {
        var id = LigneAppelOffreId.generate();
        assertThatThrownBy(() -> LigneOffre.creer(id, BigDecimal.ZERO, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("délai de livraison nul ou négatif → IllegalArgumentException")
    void delaiInvalide_leveException() {
        var id1 = LigneAppelOffreId.generate();
        var id2 = LigneAppelOffreId.generate();
        assertThatThrownBy(() -> LigneOffre.creer(id1, BigDecimal.TEN, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LigneOffre.creer(id2, BigDecimal.TEN, -5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ligne d'appel d'offres référencée obligatoire")
    void ligneAppelOffreIdNull_leveException() {
        assertThatThrownBy(() -> LigneOffre.creer(null, BigDecimal.TEN, 10))
                .isInstanceOf(NullPointerException.class);
    }
}
