package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class CodeRegionDejaUtiliseException extends ConflictException {
    public CodeRegionDejaUtiliseException(String code) {
        super("Ce code région est déjà utilisé : " + code, "REGION_CODE_ALREADY_USED");
    }
}
