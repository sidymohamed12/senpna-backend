package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CategorieActualiteInvalideException extends SenPnaException {
    public CategorieActualiteInvalideException(String valeur) {
        super("Catégorie d'actualité invalide : '" + valeur + "'", "CATEGORIE_ACTUALITE_INVALIDE", ErrorCategory.VALIDATION);
    }
}
