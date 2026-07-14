package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class OtpCooldownException extends SenPnaException {
    public OtpCooldownException(long secondesRestantes) {
        super("Veuillez patienter " + secondesRestantes + " seconde(s) avant de redemander un code",
                "OTP_COOLDOWN", ErrorCategory.BUSINESS_RULE);
    }
}
