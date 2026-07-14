package ministere.sante.senpna.appeloffre.domain.valueobject;

/**
 * Statut d'une offre soumise par un fournisseur en réponse à un appel
 * d'offres.
 *
 * <ul>
 * <li>{@code SOUMISE} : déposée par le fournisseur, en attente
 * d'analyse.</li>
 * <li>{@code RETIREE} : retirée par le fournisseur lui-même, avant la
 * clôture de l'appel d'offres.</li>
 * <li>{@code RETENUE} : sélectionnée par la PNA lors de
 * l'attribution.</li>
 * <li>{@code REJETEE} : non retenue lors de l'attribution.</li>
 * </ul>
 */
public enum StatutOffre {
    SOUMISE,
    RETIREE,
    RETENUE,
    REJETEE
}
