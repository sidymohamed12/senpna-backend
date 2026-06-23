package ministere.sante.senpna.shared.infrastructure.exception;

public class NotFoundException extends SenPnaException {

    public NotFoundException(String message, String type) {
        super(message, type);
    }
}
