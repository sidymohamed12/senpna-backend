package ministere.sante.senpna.fournisseur.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class NomFournisseurDejaUtiliseException extends ConflictException {
    public NomFournisseurDejaUtiliseException(String nom) {
        super("Un fournisseur avec le nom '" + nom + "' existe déjà", "FOURNISSEUR_NOM_ALREADY_USED");
    }
}
