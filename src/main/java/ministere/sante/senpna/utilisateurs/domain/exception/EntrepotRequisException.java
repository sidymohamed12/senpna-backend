package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class EntrepotRequisException extends ValidationException {
    public EntrepotRequisException() {
        super("Un entrepôt est obligatoire pour le(s) rôle(s) demandé(s)", "ENTREPOT_REQUIRED");
    }
}
