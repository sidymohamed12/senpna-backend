package ministere.sante.senpna.shared.domain.exception;

public class ForbiddenException extends SenPnaException {

    public ForbiddenException(String message, String type) {
        super(message, type);
    }
}
