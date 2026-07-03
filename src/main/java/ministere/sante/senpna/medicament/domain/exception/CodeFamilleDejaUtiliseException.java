package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class CodeFamilleDejaUtiliseException extends ConflictException {
    public CodeFamilleDejaUtiliseException(String code) {
        super("Une famille avec le code '" + code + "' existe déjà", "FAMILLE_CODE_ALREADY_USED");
    }
}
