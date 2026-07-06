package ministere.sante.senpna.actualite.domain.valueobject;

/**
 * Catégories éditoriales d'une actualité — sélection obligatoire à la
 * création (cf. spécification métier).
 */
public enum CategorieActualite {

    VIE_ASSOCIATIVE("Vie associative"),
    PROJET("Projet"),
    PARTENARIAT("Partenariat"),
    EVENEMENT("Événement"),
    COMMUNIQUE("Communiqué"),
    AUTRE("Autre");

    private final String libelle;

    CategorieActualite(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
