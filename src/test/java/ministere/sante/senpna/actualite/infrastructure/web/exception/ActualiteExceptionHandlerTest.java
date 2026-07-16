package ministere.sante.senpna.actualite.infrastructure.web.exception;

import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.exception.CategorieActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.StatutActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.TypeMediaInvalideException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ActualiteExceptionHandler")
class ActualiteExceptionHandlerTest {

    ActualiteExceptionHandler handler = new ActualiteExceptionHandler();

    @Test
    @DisplayName("ActualiteIntrouvableException → 404 NOT_FOUND")
    void handleActualiteIntrouvable_404() {
        ResponseEntity<Map<String, Object>> response = handler
                .handleActualiteIntrouvable(new ActualiteIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "ACTUALITE_NOT_FOUND");
        assertThat(response.getBody()).containsEntry("message", "Actualité introuvable");
    }

    @Test
    @DisplayName("CategorieActualiteInvalideException → 400 BAD_REQUEST")
    void handleCategorieInvalide_400() {
        ResponseEntity<Map<String, Object>> response = handler
                .handleCategorieInvalide(new CategorieActualiteInvalideException("INEXISTANTE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("type", "CATEGORIE_ACTUALITE_INVALIDE");
        assertThat(response.getBody().get("message").toString()).contains("INEXISTANTE");
    }

    @Test
    @DisplayName("StatutActualiteInvalideException → 400 BAD_REQUEST")
    void handleStatutInvalide_400() {
        ResponseEntity<Map<String, Object>> response = handler
                .handleStatutInvalide(new StatutActualiteInvalideException("INEXISTANT"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("type", "STATUT_ACTUALITE_INVALIDE");
        assertThat(response.getBody().get("message").toString()).contains("INEXISTANT");
    }

    @Test
    @DisplayName("TypeMediaInvalideException → 400 BAD_REQUEST")
    void handleTypeMediaInvalide_400() {
        ResponseEntity<Map<String, Object>> response = handler
                .handleTypeMediaInvalide(new TypeMediaInvalideException("AUDIO"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("type", "TYPE_MEDIA_INVALIDE");
        assertThat(response.getBody().get("message").toString()).contains("AUDIO");
    }
}
