package ministere.sante.senpna.shared.infrastructure.exception;

// 422 (Unprocessable Entity)
public class BusinessRuleException extends SenPnaException {

    public BusinessRuleException(String message, String type) {
        super(message, type);
    }
}
