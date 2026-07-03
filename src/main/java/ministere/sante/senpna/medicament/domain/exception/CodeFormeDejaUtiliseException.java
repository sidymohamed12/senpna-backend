package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class CodeFormeDejaUtiliseException extends ConflictException {
    public CodeFormeDejaUtiliseException(String code) {
        super("Une forme avec le code '" + code + "' existe déjà", "FORME_CODE_ALREADY_USED");
    }
}
