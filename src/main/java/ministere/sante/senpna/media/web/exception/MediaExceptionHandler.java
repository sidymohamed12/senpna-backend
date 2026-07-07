package ministere.sante.senpna.media.web.exception;

import ministere.sante.senpna.media.domain.exception.ContentTypeNonAutoriseException;
import ministere.sante.senpna.media.domain.exception.MediaTypeNonAutorisePubliquementException;
import ministere.sante.senpna.media.domain.exception.TailleFichierDepasseeException;
import ministere.sante.senpna.media.web.controller.MediaController;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Gestionnaire d'exceptions dédié à la feature {@code media}.
 *
 * <h3>Exceptions gérées</h3>
 * 
 * <pre>
 * ContentTypeNonAutoriseException         → 415  type MIME non supporté
 * TailleFichierDepasseeException          → 413  fichier trop volumineux
 * MediaTypeNonAutorisePubliquementException → 403  mediaType non éligible à l'endpoint public
 * </pre>
 */
@RestControllerAdvice(assignableTypes = MediaController.class)
public class MediaExceptionHandler {

        /**
         * Le Content-Type du fichier n'est pas dans la liste blanche
         * (JPEG, PNG, WebP selon le type de média).
         * HTTP 415 Unsupported Media Type.
         */
        @ExceptionHandler(ContentTypeNonAutoriseException.class)
        public ResponseEntity<Map<String, Object>> handleContentTypeNonAutorise(
                        ContentTypeNonAutoriseException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body(RestResponse.error(
                                                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                                ex.getMessage(),
                                                "CONTENT_TYPE_NOT_ALLOWED"));
        }

        /**
         * Le fichier dépasse la taille maximale autorisée pour ce type de média
         * HTTP 413 Content Too Large.
         */
        @ExceptionHandler(TailleFichierDepasseeException.class)
        public ResponseEntity<Map<String, Object>> handleTailleFichierDepassee(
                        TailleFichierDepasseeException ex) {
                return ResponseEntity
                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body(RestResponse.error(
                                                HttpStatus.PAYLOAD_TOO_LARGE,
                                                ex.getMessage(),
                                                "FILE_TOO_LARGE"));
        }

        /**
         * Le {@code mediaType} demandé sans authentification n'est pas éligible
         * à un usage public (cf. {@code POST /api/medias/presigned-url/public}).
         * HTTP 403 Forbidden.
         */
        @ExceptionHandler(MediaTypeNonAutorisePubliquementException.class)
        public ResponseEntity<Map<String, Object>> handleMediaTypeNonAutorisePubliquement(
                        MediaTypeNonAutorisePubliquementException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(RestResponse.error(
                                                HttpStatus.FORBIDDEN,
                                                ex.getMessage(),
                                                ex.getType()));
        }
}
