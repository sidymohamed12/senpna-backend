package ministere.sante.senpna.projet.infrastructure.web.exception;

import ministere.sante.senpna.projet.domain.exception.CategorieProjetInvalideException;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.exception.StatutProjetInvalideException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjetExceptionHandler — mapping des exceptions du module projet")
class ProjetExceptionHandlerTest {

    ProjetExceptionHandler sut = new ProjetExceptionHandler();

    @Test
    @DisplayName("ProjetIntrouvableException → 404")
    void projetIntrouvable_404() {
        var response = sut.handleProjetIntrouvable(new ProjetIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "PROJET_NOT_FOUND");
    }

    @Test
    @DisplayName("CategorieProjetInvalideException → 400")
    void categorieInvalide_400() {
        var response = sut.handleCategorieInvalide(new CategorieProjetInvalideException("X"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("type", "CATEGORIE_PROJET_INVALIDE");
    }

    @Test
    @DisplayName("StatutProjetInvalideException → 400")
    void statutInvalide_400() {
        var response = sut.handleStatutInvalide(new StatutProjetInvalideException("X"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("type", "STATUT_PROJET_INVALIDE");
    }
}
