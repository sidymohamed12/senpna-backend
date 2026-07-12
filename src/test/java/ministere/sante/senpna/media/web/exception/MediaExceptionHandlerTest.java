package ministere.sante.senpna.media.web.exception;

import ministere.sante.senpna.media.domain.exception.ContentTypeNonAutoriseException;
import ministere.sante.senpna.media.domain.exception.MediaTypeNonAutorisePubliquementException;
import ministere.sante.senpna.media.domain.exception.TailleFichierDepasseeException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MediaExceptionHandler — mapping des exceptions du module media")
class MediaExceptionHandlerTest {

    MediaExceptionHandler sut = new MediaExceptionHandler();

    @Test
    @DisplayName("ContentTypeNonAutoriseException → 415 avec le type d'erreur attendu")
    void contentTypeNonAutorise_415() {
        ResponseEntity<Map<String, Object>> response = sut.handleContentTypeNonAutorise(
                new ContentTypeNonAutoriseException("text/plain", "image/jpeg, image/png"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).containsEntry("type", "CONTENT_TYPE_NOT_ALLOWED");
    }

    @Test
    @DisplayName("TailleFichierDepasseeException → 413 avec le type d'erreur attendu")
    void tailleFichierDepassee_413() {
        ResponseEntity<Map<String, Object>> response = sut.handleTailleFichierDepassee(
                new TailleFichierDepasseeException(10_000_000L, 5_000_000L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).containsEntry("type", "FILE_TOO_LARGE");
    }

    @Test
    @DisplayName("MediaTypeNonAutorisePubliquementException → 403 avec le type d'erreur de l'exception")
    void mediaTypeNonAutorisePubliquement_403() {
        ResponseEntity<Map<String, Object>> response = sut.handleMediaTypeNonAutorisePubliquement(
                new MediaTypeNonAutorisePubliquementException("ACTUALITE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("type", "MEDIA_TYPE_NON_AUTORISE_PUBLIQUEMENT");
    }
}
