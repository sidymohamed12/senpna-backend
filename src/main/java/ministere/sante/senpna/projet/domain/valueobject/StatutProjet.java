package ministere.sante.senpna.projet.domain.valueobject;

/**
 * Cycle de vie éditorial d un projet.
 *
 * <pre>
 * BROUILLON --publier--&gt; PUBLIE --archiver--&gt; ARCHIVE
 *    ^                      |  |
 *    +--remettreEnBrouillon-+  +--desactiver--&gt;DESACTIVE--publier(republie)--&gt;PUBLIE
 * </pre>
 *
 * Aucune suppression physique — ARCHIVE/DESACTIVE masquent le projet sans
 * perdre l historique.
 */
public enum StatutProjet {
    BROUILLON,
    PUBLIE,
    ARCHIVE,
    DESACTIVE
}
