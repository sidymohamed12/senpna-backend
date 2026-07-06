package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class CategorieActualiteInvalideException extends ValidationException {
    public CategorieActualiteInvalideException(String valeur) {
        super("Catégorie d'actualité invalide : '" + valeur + "'", "CATEGORIE_ACTUALITE_INVALIDE");
    }
}
