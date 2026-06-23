package ministere.sante.senpna.shared.infrastructure.exception;

// 400
public class ValidationException extends SenPnaException {

    public ValidationException(String message, String type) {
        super(message, type);
    }
}
