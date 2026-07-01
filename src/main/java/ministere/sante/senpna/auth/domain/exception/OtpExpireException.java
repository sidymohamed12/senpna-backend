package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.UnauthorizedException;

public class OtpExpireException extends UnauthorizedException {
    public OtpExpireException() {
        super("Le code OTP a expiré. Veuillez en demander un nouveau.", "OTP_EXPIRED");
    }
}
