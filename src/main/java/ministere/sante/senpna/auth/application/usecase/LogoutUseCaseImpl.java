package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.command.AuthCommands.LogoutCommand;
import ministere.sante.senpna.auth.domain.port.in.LogoutUseCase;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Déconnexion : révoque immédiatement le(s) token(s) courant(s) en les
 * ajoutant à la liste noire Redis (cf. {@link TokenPort#invalider(String)}),
 * les rendant inutilisables même avant leur expiration naturelle.
 *
 * <p>
 * L'access token est toujours révoqué (il est obligatoirement fourni : la
 * route est authentifiée). Le refresh token, s'il est transmis par le
 * client, est également révoqué pour empêcher toute régénération d'un
 * nouvel access token après déconnexion.
 * </p>
 *
 * <p>
 * Idempotent et silencieux sur token déjà invalide/malformé : une
 * déconnexion ne doit jamais échouer côté client.
 * </p>
 */
@Service
public class LogoutUseCaseImpl implements LogoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogoutUseCaseImpl.class);

    private final TokenPort tokenPort;

    public LogoutUseCaseImpl(TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @Override
    public void logout(LogoutCommand command) {
        revoquerSiPresent(command.accessToken());
        revoquerSiPresent(command.refreshToken());
    }

    private void revoquerSiPresent(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            tokenPort.invalider(token);
        } catch (Exception e) {
            // Ne jamais faire échouer le logout côté client pour un token
            // déjà malformé/expiré : on logue et on continue.
            log.debug("[Logout] Révocation ignorée pour un token invalide : {}", e.getMessage());
        }
    }
}
