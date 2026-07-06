package ministere.sante.senpna.projet.domain.valueobject;

public enum CategorieProjet {

    ENVIRONNEMENT("Environnement"),
    SOCIAL("Social"),
    INNOVATION("Innovation"),
    EDUCATION("Éducation"),
    SANTE("Santé"),
    AUTRE("Autre");

    private final String libelle;

    CategorieProjet(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
