package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;

public interface RefreshTokenUseCase {
    AuthTokens rafraichir(RefreshTokenCommand command);
}
