package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CodeRegionDejaUtiliseException extends SenPnaException {
    public CodeRegionDejaUtiliseException(String code) {
        super("Ce code région est déjà utilisé : " + code, "REGION_CODE_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
