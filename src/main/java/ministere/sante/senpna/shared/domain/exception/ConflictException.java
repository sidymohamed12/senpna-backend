package ministere.sante.senpna.shared.domain.exception;

public class ConflictException extends SenPnaException {

    public ConflictException(String message, String type) {
        super(message, type);
    }
}
