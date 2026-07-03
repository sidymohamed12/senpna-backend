package ministere.sante.senpna.medicament.domain.criteria;

public record FormeSearchCriteria(String recherche, Boolean actif) {

    public static FormeSearchCriteria vide() {
        return new FormeSearchCriteria(null, null);
    }
}
