package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class OtpTentativesEpuiseesException extends SenPnaException {
    public OtpTentativesEpuiseesException() {
        super("Nombre maximal de tentatives atteint. Veuillez redemander un code.", "OTP_MAX_ATTEMPTS", ErrorCategory.FORBIDDEN);
    }
}
