package ministere.sante.senpna.stock.domain.exception.lot;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class NumeroLotDejaUtiliseException extends SenPnaException {
    public NumeroLotDejaUtiliseException(String numeroLot) {
        super("Un lot avec le numéro '" + numeroLot + "' existe déjà pour ce médicament", "LOT_NUMERO_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
