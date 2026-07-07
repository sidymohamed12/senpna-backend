package ministere.sante.senpna.carriere.domain.valueobject;

/**
 * Cycle de vie éditorial d'une opportunité de carrière.
 *
 * <pre>
 * BROUILLON → OUVERT → EN_COURS → CLOTURE
 *      ↑________________________|   (remise en brouillon possible)
 * </pre>
 *
 * <ul>
 * <li>{@code BROUILLON} — en cours de rédaction, jamais visible publiquement ;</li>
 * <li>{@code OUVERT} — publiée et ouverte aux candidatures ;</li>
 * <li>{@code EN_COURS} — traitement des candidatures en cours ; encore visible
 * publiquement mais n'accepte plus de nouvelles candidatures ;</li>
 * <li>{@code CLOTURE} — terminée, que ce soit manuellement ou automatiquement
 * du fait du dépassement de la date limite de candidature (cf.
 * {@code OpportuniteCarriere#estExpiree()} et le job planifié de clôture
 * automatique).</li>
 * </ul>
 */
public enum StatutOpportunite {
    BROUILLON,
    OUVERT,
    EN_COURS,
    CLOTURE
}
