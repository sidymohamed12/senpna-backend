package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CodeStructureSanitaireDejaUtiliseException extends SenPnaException {
    public CodeStructureSanitaireDejaUtiliseException(String code) {
        super("Ce code de structure sanitaire est déjà utilisé : " + code, "STRUCTURE_SANITAIRE_CODE_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
