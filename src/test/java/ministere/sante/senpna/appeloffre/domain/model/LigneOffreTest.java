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
        @DisplayName("prix unitaire null → NullPointerException")
        void prixNull_leveException() {
                var id = LigneAppelOffreId.generate();
                assertThatThrownBy(() -> LigneOffre.creer(id, null, 10))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("délai de livraison null → NullPointerException")
        void delaiNull_leveException() {
                var id = LigneAppelOffreId.generate();
                assertThatThrownBy(() -> LigneOffre.creer(id, BigDecimal.TEN, null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("reconstruct() restaure fidèlement l'état persisté, y compris l'identifiant")
        void reconstruct_restaureEtat() {
                ministere.sante.senpna.appeloffre.domain.valueobject.LigneOffreId id = ministere.sante.senpna.appeloffre.domain.valueobject.LigneOffreId
                                .generate();
                ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId ligneAppelOffreId = LigneAppelOffreId
                                .generate();

                LigneOffre ligne = LigneOffre.reconstruct(id, ligneAppelOffreId, BigDecimal.valueOf(1500), 20);

                assertThat(ligne.getId()).isEqualTo(id);
                assertThat(ligne.getLigneAppelOffreId()).isEqualTo(ligneAppelOffreId);
                assertThat(ligne.getPrixUnitaire()).isEqualByComparingTo("1500");
                assertThat(ligne.getDelaiLivraisonJours()).isEqualTo(20);
        }

        @Test
        @DisplayName("equals()/hashCode() basés uniquement sur l'identifiant")
        void equalsHashCode_baseSurId() {
                ministere.sante.senpna.appeloffre.domain.valueobject.LigneOffreId id = ministere.sante.senpna.appeloffre.domain.valueobject.LigneOffreId
                                .generate();
                LigneOffre ligne1 = LigneOffre.reconstruct(id, LigneAppelOffreId.generate(), BigDecimal.TEN, 10);
                LigneOffre ligne2 = LigneOffre.reconstruct(id, LigneAppelOffreId.generate(), BigDecimal.valueOf(99),
                                30);
                LigneOffre ligneAutreId = LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.TEN, 10);

                assertThat(ligne1).isEqualTo(ligne1)
                                .isEqualTo(ligne2)
                                .hasSameHashCodeAs(ligne2)
                                .isNotEqualTo(ligneAutreId)
                                .isNotEqualTo(null)
                                .isNotEqualTo("pas une LigneOffre");
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
