package ministere.sante.senpna.auth.domain.port.out;

import java.util.UUID;

public interface ResetTokenPort {

    /**
     * Génère et stocke un jeton de réinitialisation (UUID aléatoire, TTL 5 min).
     */
    String genererResetToken(UUID userId, String email);

    /**
     * Valide le jeton, le consomme (usage unique) et retourne l'identifiant
     * de l'utilisateur associé.
     */
    UUID validerEtExtraireUserId(String resetToken);

    void invaliderResetToken(String resetToken);
}
