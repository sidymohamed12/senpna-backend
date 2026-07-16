package ministere.sante.senpna.actualite.application.service;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.MediaInput;
import ministere.sante.senpna.actualite.domain.exception.CategorieActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.StatutActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.TypeMediaInvalideException;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ActualiteCommandMapper — conversion des chaînes de requête vers les enums du domaine")
class ActualiteCommandMapperTest {

    ActualiteCommandMapper sut = new ActualiteCommandMapper();

    @Test
    @DisplayName("versCategorie() convertit une valeur valide, insensible à la casse et aux espaces")
    void versCategorie_valeurValide() {
        assertThat(sut.versCategorie(" vie_associative ".toUpperCase().trim()))
                .isEqualTo(CategorieActualite.valueOf("VIE_ASSOCIATIVE"));
    }

    @Test
    @DisplayName("versCategorie() valeur null ou vide → CategorieActualiteInvalideException")
    void versCategorie_vide_leveException() {
        assertThatThrownBy(() -> sut.versCategorie(null)).isInstanceOf(CategorieActualiteInvalideException.class);
        assertThatThrownBy(() -> sut.versCategorie("   ")).isInstanceOf(CategorieActualiteInvalideException.class);
    }

    @Test
    @DisplayName("versCategorie() valeur inconnue → CategorieActualiteInvalideException")
    void versCategorie_inconnue_leveException() {
        assertThatThrownBy(() -> sut.versCategorie("PAS_UNE_CATEGORIE"))
                .isInstanceOf(CategorieActualiteInvalideException.class);
    }

    @Test
    @DisplayName("versCategorieOptionnelle() null ou vide → null, sans lever d'exception")
    void versCategorieOptionnelle_videRenvoieNull() {
        assertThat(sut.versCategorieOptionnelle(null)).isNull();
        assertThat(sut.versCategorieOptionnelle("  ")).isNull();
    }

    @Test
    @DisplayName("versCategorieOptionnelle() valeur invalide → lève tout de même l'exception")
    void versCategorieOptionnelle_invalide_leveException() {
        assertThatThrownBy(() -> sut.versCategorieOptionnelle("INVALIDE"))
                .isInstanceOf(CategorieActualiteInvalideException.class);
    }

    @Test
    @DisplayName("versStatutOptionnel() null ou vide → null")
    void versStatutOptionnel_videRenvoieNull() {
        assertThat(sut.versStatutOptionnel(null)).isNull();
        assertThat(sut.versStatutOptionnel("")).isNull();
    }

    @Test
    @DisplayName("versStatutOptionnel() valeur valide → convertie")
    void versStatutOptionnel_valeurValide() {
        assertThat(sut.versStatutOptionnel("publie")).isEqualTo(StatutActualite.valueOf("PUBLIE"));
    }

    @Test
    @DisplayName("versStatutOptionnel() valeur inconnue → StatutActualiteInvalideException")
    void versStatutOptionnel_inconnue_leveException() {
        assertThatThrownBy(() -> sut.versStatutOptionnel("PAS_UN_STATUT"))
                .isInstanceOf(StatutActualiteInvalideException.class);
    }

    @Test
    @DisplayName("versMedias() null ou vide → liste vide, sans exception")
    void versMedias_videRenvoieListeVide() {
        assertThat(sut.versMedias(null)).isEmpty();
        assertThat(sut.versMedias(List.of())).isEmpty();
    }

    @Test
    @DisplayName("versMedias() convertit chaque média et respecte l'ordre d'apparition")
    void versMedias_convertitEtOrdonne() {
        List<ActualiteMedia> medias = sut.versMedias(
                List.of(new MediaInput("image", "https://cdn/1.png"), new MediaInput("VIDEO", "https://cdn/2.mp4")));

        assertThat(medias).hasSize(2);
        assertThat(medias.get(0).getType()).isEqualTo(TypeMedia.IMAGE);
        assertThat(medias.get(0).getOrdre()).isZero();
        assertThat(medias.get(1).getType()).isEqualTo(TypeMedia.VIDEO);
        assertThat(medias.get(1).getOrdre()).isEqualTo(1);
    }

    @Test
    @DisplayName("versMedias() type de média null ou vide → TypeMediaInvalideException")
    void versMedias_typeVide_leveException() {
        List<MediaInput> medias = List.of(new MediaInput(null, "https://cdn/1.png"));
        assertThatThrownBy(() -> sut.versMedias(medias)).isInstanceOf(TypeMediaInvalideException.class);
    }

    @Test
    @DisplayName("versMedias() type de média inconnu → TypeMediaInvalideException")
    void versMedias_typeInconnu_leveException() {
        List<MediaInput> medias = List.of(new MediaInput("AUDIO", "https://cdn/1.mp3"));
        assertThatThrownBy(() -> sut.versMedias(medias)).isInstanceOf(TypeMediaInvalideException.class);
    }
}
