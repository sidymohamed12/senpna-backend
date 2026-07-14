package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class OtpExpireException extends SenPnaException {
    public OtpExpireException() {
        super("Le code OTP a expiré. Veuillez en demander un nouveau.", "OTP_EXPIRED", ErrorCategory.UNAUTHORIZED);
    }
}
