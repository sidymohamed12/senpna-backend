package ministere.sante.senpna.auth.domain.port.out;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.TokenRevocationPort;

import java.util.Set;

public interface TokenPort extends TokenRevocationPort {

    // ── Génération ────────────────────────────────────────────────────

    /**
     * Génère un access token signé contenant l'email et les codes de rôle.
     */
    String genererAccess(User user, Set<String> roleCodes);

    /**
     * Génère un refresh token, signé, sans claims métier (juste subject + type).
     */
    String genererRefresh(User user);

    // ── Révocation (liste noire Redis) ───────────────────────────────

    /**
     * Ajoute le token à la liste noire Redis.
     * TTL Redis = durée de vie résiduelle du token (auto-nettoyage garanti).
     */
    void invalider(String token);

    // ── Extraction (évite de dépendre de JwtService dans les use cases) ─

    String extraireEmail(String token);

    /**
     * @return {@code true} si le token porte le claim {@code type=refresh}
     */
    boolean estRefreshToken(String token);

    /**
     * @return {@code true} si la date d'expiration est dépassée
     */
    boolean estExpire(String token);
}
