package ministere.sante.senpna.projet.domain.criteria;

import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;

public record ProjetSearchCriteria(String recherche, CategorieProjet categorie, StatutProjet statut) {

    public static ProjetSearchCriteria vide() {
        return new ProjetSearchCriteria(null, null, null);
    }
}
