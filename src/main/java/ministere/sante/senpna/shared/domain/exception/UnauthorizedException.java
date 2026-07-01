package ministere.sante.senpna.shared.domain.exception;

public class UnauthorizedException extends SenPnaException {

    public UnauthorizedException(String message, String type) {
        super(message, type);
    }
}
