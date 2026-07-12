package ministere.sante.senpna.shared.infrastructure.web.response;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class RestResponse {

    private static final String STATUS = "status";
    private static final String TYPE = "type";
    private static final String MESSAGE = "message";
    private static final String TIMESTAMP = "timestamp";
    private static final String RESULTS = "results";
    private static final String PAGINATION = "pagination";
    private static final String ERRORS = "errors";

    private RestResponse() {
        throw new UnsupportedOperationException("Utility class — not instantiable");
    }

    public static Map<String, Object> response(
            HttpStatus status,
            Object results,
            String type,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, status.value());
        body.put(TYPE, type);
        body.put(MESSAGE, message);
        body.put(TIMESTAMP, Instant.now().toString());
        body.put(RESULTS, results);
        return body;
    }

    public static Map<String, Object> responsePaginate(
            HttpStatus status,
            Object results,
            String type,
            String message,
            int currentPage,
            int totalPages,
            long totalItems,
            boolean first,
            boolean last) {

        Map<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("currentPage", currentPage);
        pagination.put("totalPages", totalPages);
        pagination.put("totalItems", totalItems);
        pagination.put("first", first);
        pagination.put("last", last);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, status.value());
        body.put(TYPE, type);
        body.put(MESSAGE, message);
        body.put(TIMESTAMP, Instant.now().toString());
        body.put(RESULTS, results);
        body.put(PAGINATION, pagination);

        return body;
    }

    public static Map<String, Object> error(HttpStatus status, String message, String type) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, status.value());
        body.put(TYPE, type);
        body.put(MESSAGE, message);
        body.put(TIMESTAMP, Instant.now().toString());
        body.put(RESULTS, null);

        return body;
    }

    // ── Erreur de validation (@Valid) ─────────────────────────────────────
    public static Map<String, Object> validationError(BindingResult bindingResult) {
        Map<String, Object> body = error(
                HttpStatus.BAD_REQUEST,
                "Les données envoyées sont invalides",
                "VALIDATION_ERROR");

        body.put(ERRORS, extractFieldErrors(bindingResult));
        return body;
    }

    public static Map<String, String> extractFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Valeur invalide",
                        (existing, duplicate) -> existing));
    }
}