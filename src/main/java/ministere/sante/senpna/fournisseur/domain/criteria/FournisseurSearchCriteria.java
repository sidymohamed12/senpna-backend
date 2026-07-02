package ministere.sante.senpna.fournisseur.domain.criteria;

public record FournisseurSearchCriteria(String recherche, Boolean actif) {

    public static FournisseurSearchCriteria vide() {
        return new FournisseurSearchCriteria(null, null);
    }
}
