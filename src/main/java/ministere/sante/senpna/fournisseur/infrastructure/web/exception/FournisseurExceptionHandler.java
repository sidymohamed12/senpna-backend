package ministere.sante.senpna.fournisseur.infrastructure.web.exception;

import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;
import ministere.sante.senpna.fournisseur.infrastructure.web.controller.implemment.FournisseursController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code fournisseur}.
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * FournisseurIntrouvableException      → 404  fournisseur introuvable
 * NomFournisseurDejaUtiliseException   → 409  nom de fournisseur déjà utilisé
 * </pre>
 */
@RestControllerAdvice(assignableTypes = FournisseursController.class)
public class FournisseurExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(FournisseurExceptionHandler.class);

        @ExceptionHandler(FournisseurIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleFournisseurIntrouvable(FournisseurIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(NomFournisseurDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleNomDejaUtilise(NomFournisseurDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[FOURNISSEUR] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
