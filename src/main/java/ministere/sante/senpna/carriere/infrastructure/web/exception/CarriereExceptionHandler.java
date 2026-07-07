package ministere.sante.senpna.carriere.infrastructure.web.exception;

import ministere.sante.senpna.carriere.domain.exception.CandidatureIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.CiviliteInvalideException;
import ministere.sante.senpna.carriere.domain.exception.ConsentementRgpdRequisException;
import ministere.sante.senpna.carriere.domain.exception.DateLimiteCandidatureInvalideException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteFermeeException;
import ministere.sante.senpna.carriere.domain.exception.StatutOpportuniteInvalideException;
import ministere.sante.senpna.carriere.domain.exception.TypeContratInvalideException;
import ministere.sante.senpna.carriere.infrastructure.web.controller.implement.CandidaturesController;
import ministere.sante.senpna.carriere.infrastructure.web.controller.implement.OpportunitesCarriereController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code carriere} (offres
 * d'emploi et candidatures).
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * OpportuniteCarriereIntrouvableException → 404  offre introuvable
 * CandidatureIntrouvableException         → 404  candidature introuvable
 * TypeContratInvalideException            → 400  type de contrat hors énumération
 * StatutOpportuniteInvalideException      → 400  statut hors énumération
 * CiviliteInvalideException               → 400  civilité hors énumération (M/MME)
 * ConsentementRgpdRequisException         → 400  case de consentement RGPD non cochée
 * DateLimiteCandidatureInvalideException  → 400  date limite absente/postérieure à la date de début/dépassée à la publication
 * OpportuniteFermeeException              → 422  offre n'acceptant plus de nouvelles candidatures
 * </pre>
 */
@RestControllerAdvice(assignableTypes = { OpportunitesCarriereController.class, CandidaturesController.class })
public class CarriereExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(CarriereExceptionHandler.class);

        @ExceptionHandler(OpportuniteCarriereIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleOpportuniteIntrouvable(
                        OpportuniteCarriereIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(CandidatureIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleCandidatureIntrouvable(CandidatureIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(TypeContratInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleTypeContratInvalide(TypeContratInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(StatutOpportuniteInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleStatutInvalide(StatutOpportuniteInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(CiviliteInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleCiviliteInvalide(CiviliteInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(ConsentementRgpdRequisException.class)
        public ResponseEntity<Map<String, Object>> handleConsentementRgpdRequis(
                        ConsentementRgpdRequisException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(DateLimiteCandidatureInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleDateLimiteInvalide(
                        DateLimiteCandidatureInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(OpportuniteFermeeException.class)
        public ResponseEntity<Map<String, Object>> handleOpportuniteFermee(OpportuniteFermeeException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[CARRIERE] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
