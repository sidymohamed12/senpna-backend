package ministere.sante.senpna.appeloffre.infrastructure.web.exception;

import ministere.sante.senpna.appeloffre.domain.exception.AccesOffreRefuseException;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreNonPublieException;
import ministere.sante.senpna.appeloffre.domain.exception.DateClotureDepasseeException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreDejaSoumiseException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.ReferenceAppelOffreDejaUtiliseeException;
import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutAppelOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppelOffreExceptionHandler — mapping des exceptions du module appeloffre")
class AppelOffreExceptionHandlerTest {

    AppelOffreExceptionHandler sut = new AppelOffreExceptionHandler();

    @Test
    @DisplayName("AppelOffreIntrouvableException → 404")
    void appelOffreIntrouvable_404() {
        var response = sut.handleAppelOffreIntrouvable(new AppelOffreIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "APPEL_OFFRE_NOT_FOUND");
    }

    @Test
    @DisplayName("OffreFournisseurIntrouvableException → 404")
    void offreIntrouvable_404() {
        var response = sut.handleOffreIntrouvable(new OffreFournisseurIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "OFFRE_FOURNISSEUR_NOT_FOUND");
    }

    @Test
    @DisplayName("ReferenceAppelOffreDejaUtiliseeException → 409")
    void referenceDejaUtilisee_409() {
        var response = sut.handleReferenceDejaUtilisee(new ReferenceAppelOffreDejaUtiliseeException("AO-2026-0001"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("type", "APPEL_OFFRE_REFERENCE_ALREADY_USED");
        assertThat(response.getBody().get("message").toString()).contains("AO-2026-0001");
    }

    @Test
    @DisplayName("AccesOffreRefuseException → 403")
    void accesRefuse_403() {
        var response = sut.handleAccesRefuse(new AccesOffreRefuseException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("type", "OFFRE_FOURNISSEUR_ACCESS_DENIED");
    }

    @Test
    @DisplayName("AppelOffreNonPublieException → 422")
    void nonPublie_422() {
        var response = sut.handleNonPublie(new AppelOffreNonPublieException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("type", "APPEL_OFFRE_NOT_PUBLISHED");
    }

    @Test
    @DisplayName("DateClotureDepasseeException → 422")
    void dateClotureDepassee_422() {
        var response = sut.handleDateClotureDepassee(new DateClotureDepasseeException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("type", "APPEL_OFFRE_DATE_CLOTURE_DEPASSEE");
    }

    @Test
    @DisplayName("OffreDejaSoumiseException → 409")
    void offreDejaSoumise_409() {
        var response = sut.handleOffreDejaSoumise(new OffreDejaSoumiseException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("type", "OFFRE_FOURNISSEUR_ALREADY_SUBMITTED");
    }

    @Test
    @DisplayName("TransitionStatutAppelOffreInvalideException → 422")
    void transitionAppelOffreInvalide_422() {
        var response = sut.handleTransitionAppelOffreInvalide(
                new TransitionStatutAppelOffreInvalideException(StatutAppelOffre.BROUILLON, "clôturer"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("type", "APPEL_OFFRE_TRANSITION_INVALID");
    }

    @Test
    @DisplayName("TransitionStatutOffreInvalideException → 422")
    void transitionOffreInvalide_422() {
        var response = sut.handleTransitionOffreInvalide(
                new TransitionStatutOffreInvalideException(StatutOffre.RETIREE, "retenir"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("type", "OFFRE_FOURNISSEUR_TRANSITION_INVALID");
    }
}
