package ministere.sante.senpna.fournisseur.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class NomFournisseurDejaUtiliseException extends SenPnaException {
    public NomFournisseurDejaUtiliseException(String nom) {
        super("Un fournisseur avec le nom '" + nom + "' existe déjà", "FOURNISSEUR_NOM_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
