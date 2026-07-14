package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class OtpInvalideException extends SenPnaException {
    public OtpInvalideException() {
        super("Code OTP invalide", "OTP_INVALID", ErrorCategory.UNAUTHORIZED);
    }
}
