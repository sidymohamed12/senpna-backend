package ministere.sante.senpna.shared.infrastructure.web.response;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class RestResponse {

    private RestResponse() {
        throw new UnsupportedOperationException("Utility class — not instantiable");
    }

    public static Map<String, Object> response(
            HttpStatus status,
            Object results,
            String type,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("type", type);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        body.put("results", results);
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
        body.put("status", status.value());
        body.put("type", type);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        body.put("results", results);
        body.put("pagination", pagination);
        return body;
    }

    public static Map<String, Object> error(HttpStatus status, String message, String type) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("type", type);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        body.put("results", null);
        return body;
    }

    // ── Erreur de validation (@Valid) ─────────────────────────────────────
    public static Map<String, Object> validationError(BindingResult bindingResult) {
        Map<String, Object> body = error(
                HttpStatus.BAD_REQUEST,
                "Les données envoyées sont invalides",
                "VALIDATION_ERROR");

        body.put("errors", extractFieldErrors(bindingResult));
        return body;
    }

    public static Map<String, String> extractFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Valeur invalide",
                        (existing, duplicate) -> existing // garde la première erreur par champ
                ));
    }
}