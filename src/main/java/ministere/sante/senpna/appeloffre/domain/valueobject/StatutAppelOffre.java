package ministere.sante.senpna.appeloffre.domain.valueobject;

/**
 * Cycle de vie d'un appel d'offres.
 *
 * <pre>
 * BROUILLON → PUBLIE → CLOTURE → ATTRIBUE
 *     │           │        │
 *     └───────────┴────────┴──────→ ANNULE
 * </pre>
 *
 * <ul>
 * <li>{@code BROUILLON} : en cours de constitution par la PNA, invisible
 * des fournisseurs.</li>
 * <li>{@code PUBLIE} : visible et ouvert à la soumission d'offres par les
 * fournisseurs actifs, jusqu'à {@code dateCloture}.</li>
 * <li>{@code CLOTURE} : la date de clôture est dépassée (ou clôture
 * manuelle anticipée) — plus aucune offre ne peut être soumise ou
 * retirée ; la PNA peut désormais analyser les offres reçues.</li>
 * <li>{@code ATTRIBUE} : la PNA a statué sur les offres reçues — statut
 * terminal.</li>
 * <li>{@code ANNULE} : annulé par la PNA avant attribution — statut
 * terminal.</li>
 * </ul>
 */
public enum StatutAppelOffre {
    BROUILLON,
    PUBLIE,
    CLOTURE,
    ATTRIBUE,
    ANNULE
}
