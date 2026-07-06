package ministere.sante.senpna.actualite.domain.valueobject;

/**
 * Cycle de vie éditorial d'une actualité.
 *
 * <pre>
 * BROUILLON  ──publier──►  PUBLIE
 *    ▲                        │
 *    └──remettreEnBrouillon───┤
 *                             │
 *                        desactiver
 *                             │
 *                             ▼
 *                        DESACTIVE ──publier──► PUBLIE
 * </pre>
 * 
 */
public enum StatutActualite {

    /** Contenu en cours de rédaction — invisible du grand public. */
    BROUILLON,

    /** Contenu visible publiquement. */
    PUBLIE,

    /** Contenu masqué — retiré de la diffusion sans être supprimé. */
    DESACTIVE
}
