package ministere.sante.senpna.stock.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class NumeroLotDejaUtiliseException extends ConflictException {
    public NumeroLotDejaUtiliseException(String numeroLot) {
        super("Un lot avec le numéro '" + numeroLot + "' existe déjà pour ce médicament", "LOT_NUMERO_ALREADY_USED");
    }
}
