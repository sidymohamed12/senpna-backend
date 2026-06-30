package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.MeQuery;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;

public interface MeUseCase {
    UserSummary me(MeQuery query);
}
