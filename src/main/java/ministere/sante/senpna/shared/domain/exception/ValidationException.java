package ministere.sante.senpna.shared.domain.exception;

// 400
public class ValidationException extends SenPnaException {

    public ValidationException(String message, String type) {
        super(message, type);
    }
}
