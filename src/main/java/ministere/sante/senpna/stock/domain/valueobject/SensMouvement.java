package ministere.sante.senpna.stock.domain.valueobject;

/**
 * Sens d'un mouvement de stock — porte l'effet sur la quantité disponible
 * de l'entrepôt concerné (cf. modèle métier complémentaire §2 « Mouvements
 * de stock ») : une entrée augmente le stock de l'entrepôt destination,
 * une sortie diminue le stock de l'entrepôt source.
 */
public enum SensMouvement {
    ENTREE,
    SORTIE
}
