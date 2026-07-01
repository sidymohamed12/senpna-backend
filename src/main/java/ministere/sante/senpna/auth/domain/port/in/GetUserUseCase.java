package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.GetUserQuery;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;

public interface GetUserUseCase {
    UserDetail obtenir(GetUserQuery query);
}
