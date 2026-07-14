package ministere.sante.senpna.shared.infrastructure.exception;

import jakarta.validation.ConstraintViolationException;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
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
 * SenPnaException (cf. ErrorCategory)
 *   UNAUTHORIZED                          → 401  token invalide, credentials incorrects
 *   FORBIDDEN                             → 403  accès refusé (métier)
 *   NOT_FOUND                             → 404  ressource introuvable (générique)
 *   CONFLICT                              → 409  conflit (générique)
 *   BUSINESS_RULE                         → 422  règle métier (générique)
 *   VALIDATION                            → 400  validation (générique)
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
 *
 * <p>
 * Toutes les exceptions métier ({@code SenPnaException} et ses
 * descendants directs) sont traitées par un unique handler qui bascule
 * sur {@link ErrorCategory} plutôt que par un handler par sous-classe
 * intermédiaire — cf. {@link ErrorCategory} pour le raisonnement
 * (correction de la règle SonarQube {@code java:S110}, profondeur
 * d'héritage). Un handler de feature scoped (via
 * {@code @RestControllerAdvice(assignableTypes = ...)}) qui déclare son
 * propre {@code @ExceptionHandler(XxxException.class)} reste toujours
 * prioritaire sur celui-ci pour les contrôleurs qu'il couvre — ce
 * changement ne modifie que le fallback générique, pas l'ordre de
 * résolution de Spring.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // ── Exceptions métier — dispatch par catégorie ─────────────────────────

        @ExceptionHandler(SenPnaException.class)
        public ResponseEntity<Map<String, Object>> handleSenPnaException(SenPnaException ex) {
                HttpStatus status = toHttpStatus(ex.getCategory());
                return ResponseEntity
                                .status(status)
                                .body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }

        private HttpStatus toHttpStatus(ErrorCategory category) {
                return switch (category) {
                        case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
                        case FORBIDDEN -> HttpStatus.FORBIDDEN;
                        case NOT_FOUND -> HttpStatus.NOT_FOUND;
                        case CONFLICT -> HttpStatus.CONFLICT;
                        case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_ENTITY;
                        case VALIDATION -> HttpStatus.BAD_REQUEST;
                };
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
         * Distinct d'une {@link SenPnaException} de catégorie {@code FORBIDDEN} :
         * ici c'est Spring qui décide, pas le code métier.
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