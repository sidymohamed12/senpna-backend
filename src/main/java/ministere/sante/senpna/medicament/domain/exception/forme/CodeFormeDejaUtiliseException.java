package ministere.sante.senpna.medicament.domain.exception.forme;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CodeFormeDejaUtiliseException extends SenPnaException {
    public CodeFormeDejaUtiliseException(String code) {
        super("Une forme avec le code '" + code + "' existe déjà", "FORME_CODE_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
