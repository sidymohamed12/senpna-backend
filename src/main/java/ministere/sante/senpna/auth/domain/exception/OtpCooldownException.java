package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

public class OtpCooldownException extends BusinessRuleException {
    public OtpCooldownException(long secondesRestantes) {
        super("Veuillez patienter " + secondesRestantes + " seconde(s) avant de redemander un code",
                "OTP_COOLDOWN");
    }
}
