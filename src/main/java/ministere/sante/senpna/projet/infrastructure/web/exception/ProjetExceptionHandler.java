package ministere.sante.senpna.projet.infrastructure.web.exception;

import ministere.sante.senpna.projet.domain.exception.CategorieProjetInvalideException;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.exception.StatutProjetInvalideException;
import ministere.sante.senpna.projet.infrastructure.web.controller.implement.ProjetsController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code projet}.
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * ProjetIntrouvableException       → 404  projet introuvable
 * CategorieProjetInvalideException → 400  catégorie hors énumération
 * StatutProjetInvalideException    → 400  statut hors énumération
 * </pre>
 */
@RestControllerAdvice(assignableTypes = ProjetsController.class)
public class ProjetExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(ProjetExceptionHandler.class);

        @ExceptionHandler(ProjetIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleProjetIntrouvable(ProjetIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(CategorieProjetInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleCategorieInvalide(CategorieProjetInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(StatutProjetInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleStatutInvalide(StatutProjetInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[PROJET] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
