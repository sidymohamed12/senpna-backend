package ministere.sante.senpna.auth.infrastructure.web;

import ministere.sante.senpna.auth.domain.exception.OtpCooldownException;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Handler dédié à la feature {@code auth}.
 *
 * <p>
 * La majorité des exceptions du domaine {@code auth} héritent directement
 * des exceptions génériques de {@code shared} ({@code UnauthorizedException},
 * {@code ForbiddenException}, {@code NotFoundException},
 * {@code BusinessRuleException})
 * et sont donc déjà gérées par {@code GlobalExceptionHandler}. Seul
 * {@link OtpCooldownException} nécessite un header HTTP spécifique
 * ({@code Retry-After}), d'où ce handler dédié.
 * </p>
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(OtpCooldownException.class)
    public ResponseEntity<Map<String, Object>> handleOtpCooldown(OtpCooldownException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(RestResponse.error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), ex.getType()));
    }
}
