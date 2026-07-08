package ministere.sante.senpna.actualite.infrastructure.web.exception;

import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.exception.CategorieActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.StatutActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.TypeMediaInvalideException;
import ministere.sante.senpna.actualite.infrastructure.web.controller.implement.ActualitesController;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Gestionnaire d'exceptions dédié à la feature {@code actualite}.
 *
 * <p>
 * Scope restreint au contrôleur de la feature ({@code assignableTypes})
 * plutôt qu'un {@code @RestControllerAdvice} global — cf.
 * {@code GlobalExceptionHandler}, qui ne couvre que les exceptions
 * techniques transversales (Spring MVC/Security, Bean Validation) et
 * délègue explicitement le métier aux handlers de chaque feature.
 * </p>
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * ActualiteIntrouvableException       → 404  actualité introuvable
 * CategorieActualiteInvalideException → 400  catégorie hors énumération
 * StatutActualiteInvalideException    → 400  statut hors énumération
 * TypeMediaInvalideException          → 400  type de média hors énumération
 * </pre>
 */
@RestControllerAdvice(assignableTypes = ActualitesController.class)
public class ActualiteExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(ActualiteExceptionHandler.class);

        @ExceptionHandler(ActualiteIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleActualiteIntrouvable(ActualiteIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(CategorieActualiteInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleCategorieInvalide(CategorieActualiteInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(StatutActualiteInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleStatutInvalide(StatutActualiteInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(TypeMediaInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleTypeMediaInvalide(TypeMediaInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[ACTUALITE] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
