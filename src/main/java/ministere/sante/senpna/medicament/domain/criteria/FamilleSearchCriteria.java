package ministere.sante.senpna.medicament.domain.criteria;

public record FamilleSearchCriteria(String recherche, Boolean actif) {

    public static FamilleSearchCriteria vide() {
        return new FamilleSearchCriteria(null, null);
    }
}
