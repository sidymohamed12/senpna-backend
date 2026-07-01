package ministere.sante.senpna.shared.domain.exception;

public abstract class SenPnaException extends RuntimeException {

    private final String type;

    protected SenPnaException(String message, String type) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
