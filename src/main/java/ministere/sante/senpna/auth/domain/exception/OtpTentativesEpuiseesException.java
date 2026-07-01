package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ForbiddenException;

public class OtpTentativesEpuiseesException extends ForbiddenException {
    public OtpTentativesEpuiseesException() {
        super("Nombre maximal de tentatives atteint. Veuillez redemander un code.", "OTP_MAX_ATTEMPTS");
    }
}
