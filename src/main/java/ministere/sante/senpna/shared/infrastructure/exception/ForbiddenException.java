package ministere.sante.senpna.shared.infrastructure.exception;

public class ForbiddenException extends SenPnaException {

    public ForbiddenException(String message, String type) {
        super(message, type);
    }
}
