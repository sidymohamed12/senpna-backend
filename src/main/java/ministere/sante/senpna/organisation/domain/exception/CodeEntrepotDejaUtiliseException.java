package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CodeEntrepotDejaUtiliseException extends SenPnaException {
    public CodeEntrepotDejaUtiliseException(String code) {
        super("Ce code d'entrepôt est déjà utilisé : " + code, "ENTREPOT_CODE_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
