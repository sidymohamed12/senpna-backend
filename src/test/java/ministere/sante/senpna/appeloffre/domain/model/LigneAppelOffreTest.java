package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LigneAppelOffre — entité de domaine")
class LigneAppelOffreTest {

    @Test
    @DisplayName("crée une ligne avec la désignation nettoyée des espaces")
    void creer_succes() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "  Paracétamol  ", BigDecimal.TEN,
                "Comprimé");

        assertThat(ligne.getDesignation()).isEqualTo("Paracétamol");
        assertThat(ligne.getQuantiteEstimee()).isEqualTo(BigDecimal.TEN);
        assertThat(ligne.getId()).isNotNull();
    }

    @Test
    @DisplayName("médicament null → NullPointerException")
    void medicamentNull_leveException() {
        assertThatThrownBy(() -> LigneAppelOffre.creer(null, "Paracétamol", BigDecimal.TEN, "Comprimé"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("désignation vide → IllegalArgumentException")
    void designationVide_leveException() {
        var medicamentId = MedicamentId.generate();
        assertThatThrownBy(() -> LigneAppelOffre.creer(medicamentId, "   ", BigDecimal.TEN, "Comprimé"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("quantité estimée nulle ou négative → IllegalArgumentException")
    void quantiteNegativeOuNulle_leveException() {
        var medicamentId = MedicamentId.generate();

        assertThatThrownBy(() -> LigneAppelOffre.creer(medicamentId, "Paracétamol", BigDecimal.ZERO,
                "Comprimé")).isInstanceOf(IllegalArgumentException.class);

        var medicamentId2 = MedicamentId.generate();
        var qte = BigDecimal.valueOf(-1);
        assertThatThrownBy(() -> LigneAppelOffre.creer(medicamentId2, "Paracétamol",
                qte, "Comprimé")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("unité de base null → NullPointerException")
    void uniteBaseNull_leveException() {
        var medicamentId = MedicamentId.generate();

        assertThatThrownBy(() -> LigneAppelOffre.creer(medicamentId, "Paracétamol", BigDecimal.TEN, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("equals()/hashCode() basés sur l'identifiant")
    void equalsHashCode_baseSurId() {
        LigneAppelOffre ligne1 = LigneAppelOffre.creer(MedicamentId.generate(), "A", BigDecimal.ONE, "u");
        LigneAppelOffre ligne2 = LigneAppelOffre.creer(MedicamentId.generate(), "A", BigDecimal.ONE, "u");

        assertThat(ligne1).isNotEqualTo(ligne2)
                .isEqualTo(ligne1)
                .isNotEqualTo(null)
                .isNotEqualTo("pas une LigneAppelOffre");
    }

    @Test
    @DisplayName("reconstruct() restaure fidèlement l'état persisté, y compris l'identifiant")
    void reconstruct_restaureEtat() {
        ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId id = ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId
                .generate();
        MedicamentId medicamentId = MedicamentId.generate();

        LigneAppelOffre ligne = LigneAppelOffre.reconstruct(id, medicamentId, "Amoxicilline", BigDecimal.TEN,
                "Comprimé");

        assertThat(ligne.getId()).isEqualTo(id);
        assertThat(ligne.getMedicamentId()).isEqualTo(medicamentId);
        assertThat(ligne.getDesignation()).isEqualTo("Amoxicilline");
        assertThat(ligne.getQuantiteEstimee()).isEqualByComparingTo("10");
        assertThat(ligne.getUniteBase()).isEqualTo("Comprimé");
    }
}
