package ministere.sante.senpna.auth.infrastructure.web;

import ministere.sante.senpna.auth.domain.exception.OtpCooldownException;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(OtpCooldownException.class)
    public ResponseEntity<Map<String, Object>> handleOtpCooldown(OtpCooldownException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(RestResponse.error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), ex.getType()));
    }
}
