package ministere.sante.senpna.stock.domain.valueobject;

/**
 * Nature d'un mouvement de stock (cf. modèle métier complémentaire §3
 * « Types de mouvement »).
 *
 * <p>
 * Le type qualifie la <em>raison</em> du mouvement — il ne détermine pas à
 * lui seul le sens (entrée/sortie) : {@link SensMouvement} porte cette
 * information séparément, car certains types ({@code AJUSTEMENT},
 * {@code INVENTAIRE}, {@code RETOUR}) peuvent se produire dans les deux
 * sens selon le contexte (un ajustement peut aussi bien corriger un
 * surplus qu'un manquant).
 * </p>
 */
public enum TypeMouvement {
    ENTREE_ACHAT,
    ENTREE_TRANSFERT,
    SORTIE_TRANSFERT,
    SORTIE_STRUCTURE,
    RETOUR,
    AJUSTEMENT,
    PERTE,
    CASSE,
    VOL,
    PEREMPTION,
    INVENTAIRE,
    DON
}
