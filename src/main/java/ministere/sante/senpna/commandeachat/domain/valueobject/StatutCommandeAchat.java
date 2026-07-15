package ministere.sante.senpna.commandeachat.domain.valueobject;

/**
 * Cycle de vie d'un bon de commande d'achat fournisseur (cf. doc. métier
 * flows CAS 1) :
 *
 * <pre>
 * EN_ATTENTE_VALIDATION → VALIDEE → EN_TRANSIT → EXPEDIEE → PARTIELLEMENT_RECEPTIONNEE → RECEPTIONNEE
 *          │                 │          │
 *          └─ REJETEE        └──────────┴──────────────────────────→ ANNULEE
 * </pre>
 *
 * <ul>
 * <li>{@code EN_ATTENTE_VALIDATION} : créée par la PNA, en attente de
 * validation interne (Pharmacien Responsable / Direction Générale /
 * Direction Financière — cf. doc. métier §e).</li>
 * <li>{@code VALIDEE} : validée en interne — visible et actionnable par
 * le fournisseur dans son espace (accusé de réception, confirmation de
 * délai).</li>
 * <li>{@code REJETEE} : refusée en validation interne — statut
 * terminal.</li>
 * <li>{@code EN_TRANSIT} : le fournisseur a confirmé le délai de
 * livraison — préparation en cours côté fournisseur.</li>
 * <li>{@code EXPEDIEE} : avis d'expédition émis par le fournisseur (lots,
 * dates de péremption, transporteur) — la commande ne peut plus être
 * modifiée ni annulée à partir de cet état (cf. règles métier).</li>
 * <li>{@code PARTIELLEMENT_RECEPTIONNEE} : une réception a eu lieu côté
 * PNA mais toutes les lignes ne sont pas complètement réceptionnées.</li>
 * <li>{@code RECEPTIONNEE} : toutes les lignes sont complètement
 * réceptionnées — statut terminal.</li>
 * <li>{@code ANNULEE} : annulée avant expédition — statut terminal.</li>
 * </ul>
 */
public enum StatutCommandeAchat {
    EN_ATTENTE_VALIDATION,
    VALIDEE,
    REJETEE,
    EN_TRANSIT,
    EXPEDIEE,
    PARTIELLEMENT_RECEPTIONNEE,
    RECEPTIONNEE,
    ANNULEE
}
