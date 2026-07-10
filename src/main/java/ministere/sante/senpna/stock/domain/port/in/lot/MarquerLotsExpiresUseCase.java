package ministere.sante.senpna.stock.domain.port.in.lot;

/**
 * Détecte et marque {@code EXPIRE} tous les lots {@code ACTIF} dont la
 * date d'expiration est dépassée — invoqué par le job planifié quotidien
 * (cf. {@code LotExpirationScheduler}).
 */
public interface MarquerLotsExpiresUseCase {
    /** @return le nombre de lots marqués expirés lors de cette exécution. */
    int marquerExpires();
}
