package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class CodeMedicamentDejaUtiliseException extends ConflictException {
    public CodeMedicamentDejaUtiliseException(String code) {
        super("Un médicament avec le code '" + code + "' existe déjà", "MEDICAMENT_CODE_ALREADY_USED");
    }
}
