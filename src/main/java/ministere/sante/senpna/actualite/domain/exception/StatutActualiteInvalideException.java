package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class StatutActualiteInvalideException extends ValidationException {
    public StatutActualiteInvalideException(String valeur) {
        super("Statut d'actualité invalide : '" + valeur + "'", "STATUT_ACTUALITE_INVALIDE");
    }
}
