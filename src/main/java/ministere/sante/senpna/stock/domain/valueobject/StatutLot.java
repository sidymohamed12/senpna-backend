package ministere.sante.senpna.stock.domain.valueobject;

/**
 * Statut d'un lot de médicament.
 *
 * <ul>
 * <li>{@code ACTIF} : le lot peut être réservé et expédié normalement.</li>
 * <li>{@code BLOQUE} : mis en quarantaine (ex : suite à une déclaration de
 * pharmacovigilance) — ne peut plus être réservé ni expédié tant qu'il
 * n'est pas explicitement débloqué.</li>
 * <li>{@code EXPIRE} : la date d'expiration est dépassée — statut terminal,
 * positionné automatiquement, ne peut plus être réservé ni expédié.</li>
 * </ul>
 */
public enum StatutLot {
    ACTIF,
    BLOQUE,
    EXPIRE
}
