package ministere.sante.senpna.auth.infrastructure.web.exception;

import ministere.sante.senpna.auth.domain.exception.CompteInactifException;
import ministere.sante.senpna.auth.domain.exception.CompteVerrouilleException;
import ministere.sante.senpna.auth.domain.exception.InvalidCredentialsException;
import ministere.sante.senpna.auth.domain.exception.InvalidRefreshTokenException;
import ministere.sante.senpna.auth.domain.exception.OtpCooldownException;
import ministere.sante.senpna.auth.domain.exception.OtpEnvoiEchoueException;
import ministere.sante.senpna.auth.domain.exception.OtpExpireException;
import ministere.sante.senpna.auth.domain.exception.OtpInvalideException;
import ministere.sante.senpna.auth.domain.exception.OtpTentativesEpuiseesException;
import ministere.sante.senpna.auth.domain.exception.ResetTokenInvalideException;
import ministere.sante.senpna.auth.infrastructure.web.controller.implement.AuthController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code auth}.
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * InvalidCredentialsException      → 401  identifiants incorrects
 * OtpInvalideException             → 401  code OTP incorrect
 * OtpExpireException               → 401  code OTP expiré
 * InvalidRefreshTokenException     → 401  refresh token invalide/expiré
 * ResetTokenInvalideException      → 401  token de réinitialisation invalide/expiré
 * CompteInactifException           → 403  compte désactivé
 * CompteVerrouilleException        → 403  compte verrouillé (tentatives échouées)
 * OtpTentativesEpuiseesException   → 403  tentatives de saisie OTP épuisées
 * OtpCooldownException             → 422  nouvelle demande d'OTP trop rapprochée
 * OtpEnvoiEchoueException          → 422  échec technique d'envoi de l'OTP (SMS/e-mail)
 * </pre>
 */
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
                return build(HttpStatus.UNAUTHORIZED, ex);
        }

        @ExceptionHandler(OtpInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleOtpInvalide(OtpInvalideException ex) {
                return build(HttpStatus.UNAUTHORIZED, ex);
        }

        @ExceptionHandler(OtpExpireException.class)
        public ResponseEntity<Map<String, Object>> handleOtpExpire(OtpExpireException ex) {
                return build(HttpStatus.UNAUTHORIZED, ex);
        }

        @ExceptionHandler(InvalidRefreshTokenException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
                return build(HttpStatus.UNAUTHORIZED, ex);
        }

        @ExceptionHandler(ResetTokenInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleResetTokenInvalide(ResetTokenInvalideException ex) {
                return build(HttpStatus.UNAUTHORIZED, ex);
        }

        @ExceptionHandler(CompteInactifException.class)
        public ResponseEntity<Map<String, Object>> handleCompteInactif(CompteInactifException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(CompteVerrouilleException.class)
        public ResponseEntity<Map<String, Object>> handleCompteVerrouille(CompteVerrouilleException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(OtpTentativesEpuiseesException.class)
        public ResponseEntity<Map<String, Object>> handleOtpTentativesEpuisees(OtpTentativesEpuiseesException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(OtpCooldownException.class)
        public ResponseEntity<Map<String, Object>> handleOtpCooldown(OtpCooldownException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(OtpEnvoiEchoueException.class)
        public ResponseEntity<Map<String, Object>> handleOtpEnvoiEchoue(OtpEnvoiEchoueException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[AUTH] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
