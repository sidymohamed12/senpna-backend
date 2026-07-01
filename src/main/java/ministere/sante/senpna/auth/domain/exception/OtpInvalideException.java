package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.UnauthorizedException;

public class OtpInvalideException extends UnauthorizedException {
    public OtpInvalideException() {
        super("Code OTP invalide", "OTP_INVALID");
    }
}
