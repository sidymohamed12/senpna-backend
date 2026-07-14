package ministere.sante.senpna.medicament.domain.exception.famille;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CodeFamilleDejaUtiliseException extends SenPnaException {
    public CodeFamilleDejaUtiliseException(String code) {
        super("Une famille avec le code '" + code + "' existe déjà", "FAMILLE_CODE_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
