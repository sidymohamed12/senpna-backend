package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class CodeEntrepotDejaUtiliseException extends ConflictException {
    public CodeEntrepotDejaUtiliseException(String code) {
        super("Ce code d'entrepôt est déjà utilisé : " + code, "ENTREPOT_CODE_ALREADY_USED");
    }
}
