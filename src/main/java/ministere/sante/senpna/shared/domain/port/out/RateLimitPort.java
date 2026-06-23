package ministere.sante.senpna.shared.domain.port.out;

/**
 * Port de sortie — rate limiting par clé (IP, userId, endpoint…).
 *
 * <p>
 * Contrat : chaque implémentation gère une fenêtre glissante ou un
 * token-bucket en mémoire partagée (Redis en prod, map locale en test).
 * </p>
 */
public interface RateLimitPort {

    /**
     * Tente de consommer un « jeton » pour la clé donnée.
     *
     * @param key      clé unique identifiant le compteur (ex: "login:192.168.1.1")
     * @param limit    nombre maximum de requêtes autorisées sur la fenêtre
     * @param windowMs durée de la fenêtre en millisecondes
     * @return résultat contenant l'autorisation et les headers de diagnostic
     */
    RateLimitResult tryConsume(String key, int limit, long windowMs);

    /**
     * Résultat d'une tentative de rate limiting.
     *
     * @param allowed         true si la requête est autorisée
     * @param remainingTokens nombre de tokens restants dans la fenêtre
     * @param resetAfterMs    millisecondes avant la réinitialisation de la fenêtre
     */
    record RateLimitResult(boolean allowed, long remainingTokens, long resetAfterMs) {

        public static RateLimitResult allow(long remaining, long resetAfterMs) {
            return new RateLimitResult(true, remaining, resetAfterMs);
        }

        public static RateLimitResult deny(long resetAfterMs) {
            return new RateLimitResult(false, 0, resetAfterMs);
        }
    }
}
