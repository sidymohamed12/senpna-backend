package ministere.sante.senpna.shared.infrastructure.exception;

import jakarta.validation.ConstraintViolationException;
import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;
import ministere.sante.senpna.shared.domain.exception.ConflictException;
import ministere.sante.senpna.shared.domain.exception.ForbiddenException;
import ministere.sante.senpna.shared.domain.exception.NotFoundException;
import ministere.sante.senpna.shared.domain.exception.UnauthorizedException;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * Gestionnaire global des exceptions HTTP — infrastructure et framework
 * uniquement.
 *
 * <h3>Responsabilité</h3>
 * <p>
 * Ce handler gère exclusivement les exceptions techniques transversales :
 * Spring MVC, Bean Validation, Spring Security. Il ne gère <em>pas</em>
 * les exceptions métier spécifiques à chaque feature — celles-ci sont
 * déléguées aux {@code @RestControllerAdvice} dédiés :
 * </p>
 * 
 * <h3>Hiérarchie de traitement (exceptions infrastructure)</h3>
 * 
 * <pre>
 * UnauthorizedException                  → 401  token invalide, credentials incorrects
 * ForbiddenException                     → 403  accès refusé (métier)
 * NotFoundException                      → 404  ressource introuvable (générique)
 * ConflictException                      → 409  conflit (générique)
 * BusinessRuleException                  → 422  règle métier (générique)
 * ValidationException                    → 400  validation (générique)
 * MethodArgumentNotValidException        → 400  @Valid sur @RequestBody
 * ConstraintViolationException           → 400  @Validated sur @RequestParam
 * NoResourceFoundException               → 404  route inexistante
 * HttpMessageNotReadableException        → 400  JSON malformé
 * HttpRequestMethodNotSupportedException → 405  méthode HTTP non supportée
 * MissingServletRequestParameterException→ 400  paramètre @RequestParam absent
 * MethodArgumentTypeMismatchException    → 400  type de paramètre incorrect
 * AuthorizationDeniedException           → 403  Spring Security @PreAuthorize
 * Exception                              → 500  fallback inattendu
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // ── Exceptions shared/métier génériques ───────────────────────────────

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(RestResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getType()));
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(RestResponse.error(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getType()));
        }

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(RestResponse.error(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getType()));
        }

        @ExceptionHandler(BusinessRuleException.class)
        public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(RestResponse.error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(),
                                                ex.getType()));
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(RestResponse.error(HttpStatus.CONFLICT, ex.getMessage(), ex.getType()));
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(RestResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getType()));
        }

        // ── Bean Validation — @Valid sur @RequestBody ──────────────────────────

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(RestResponse.validationError(ex.getBindingResult()));
        }

        // ── Bean Validation — @Min/@Max sur @RequestParam (@Validated) ─────────

        /**
         * Gère les violations de contraintes sur les paramètres de méthode.
         *
         * <p>
         * {@code @Validated} + contraintes sur paramètres ({@code @Min}, {@code @Max},
         * {@code @NotBlank}...) lèvent {@link ConstraintViolationException}, pas
         * {@link MethodArgumentNotValidException}. Sans ce handler, Spring retourne
         * HTTP 500 avec une stacktrace — comportement inacceptable en production.
         * </p>
         *
         * <h3>Format du message</h3>
         * <p>
         * Le chemin de la propriété inclut le nom de la méthode Java
         * (ex: {@code "lister.page"}). On extrait la dernière partie
         * ({@code "page"}) pour un message d'erreur lisible.
         * </p>
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Map<String, Object>> handleConstraintViolation(
                        ConstraintViolationException ex) {

                String message = ex.getConstraintViolations().stream()
                                .map(cv -> {
                                        String path = cv.getPropertyPath().toString();
                                        String param = path.contains(".")
                                                        ? path.substring(path.lastIndexOf('.') + 1)
                                                        : path;
                                        return "'" + param + "' : " + cv.getMessage();
                                })
                                .sorted()
                                .reduce((a, b) -> a + " ; " + b)
                                .orElse("Paramètre invalide");

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(RestResponse.error(HttpStatus.BAD_REQUEST, message, "INVALID_PARAMETER"));
        }

        // ── Exceptions Spring MVC ──────────────────────────────────────────────

        /**
         * Route inexistante — Spring Boot 3.2+ lance NoResourceFoundException.
         *
         * <p>
         * Exemples :
         * <ul>
         * <li>GET /api/routeInexistante</li>
         * <li>POST /api/commandesss (faute de frappe)</li>
         * </ul>
         * </p>
         */
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(RestResponse.error(
                                                HttpStatus.NOT_FOUND,
                                                "La route " + ex.getHttpMethod() + " /" + ex.getResourcePath()
                                                                + " n'existe pas",
                                                "ROUTE_NOT_FOUND"));
        }

        /**
         * Corps JSON illisible — JSON malformé, type de champ incompatible, etc.
         * Ex : envoyer {@code { "prix": "abc" }} pour un champ BigDecimal.
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
                        HttpMessageNotReadableException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(RestResponse.error(
                                                HttpStatus.BAD_REQUEST,
                                                "Le corps de la requête est illisible ou malformé",
                                                "MALFORMED_JSON"));
        }

        /**
         * Méthode HTTP non supportée.
         * Ex : POST /api/catalogue alors que seul GET est défini.
         */
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex) {
                return ResponseEntity
                                .status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(RestResponse.error(
                                                HttpStatus.METHOD_NOT_ALLOWED,
                                                "La méthode " + ex.getMethod()
                                                                + " n'est pas supportée pour cette route",
                                                "METHOD_NOT_ALLOWED"));
        }

        /**
         * Paramètre de requête obligatoire absent.
         * Ex : GET /api/catalogue sans {@code ?page=} alors que {@code required=true}.
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<Map<String, Object>> handleMissingParam(
                        MissingServletRequestParameterException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(RestResponse.error(
                                                HttpStatus.BAD_REQUEST,
                                                "Le paramètre '" + ex.getParameterName() + "' est obligatoire",
                                                "MISSING_PARAMETER"));
        }

        /**
         * Type de paramètre de chemin incorrect.
         * Ex : GET /api/produits/abc alors que {@code {id}} est un UUID.
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Map<String, Object>> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex) {
                String expectedType = ex.getRequiredType() != null
                                ? ex.getRequiredType().getSimpleName()
                                : "inconnu";
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(RestResponse.error(
                                                HttpStatus.BAD_REQUEST,
                                                "Le paramètre '" + ex.getName() + "' doit être de type " + expectedType,
                                                "INVALID_PARAMETER_TYPE"));
        }

        // ── Spring Security @PreAuthorize ──────────────────────────────────────

        /**
         * HTTP 403 levé par Spring Security quand {@code @PreAuthorize} échoue.
         * Distinct de {@link ForbiddenException} : ici c'est Spring qui décide,
         * pas le code métier.
         */
        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(
                        AuthorizationDeniedException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(RestResponse.error(HttpStatus.FORBIDDEN, "Accès refusé", "ACCESS_DENIED"));
        }

        // ── Fallback ───────────────────────────────────────────────────────────

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
                log.error("Erreur inattendue [{}] : {}", ex.getClass().getName(), ex.getMessage(), ex);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(RestResponse.error(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Une erreur interne s'est produite. Veuillez réessayer ou contacter le support.",
                                                "INTERNAL_ERROR"));
        }
}