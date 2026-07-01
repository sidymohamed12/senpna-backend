package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.GetUserQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

public interface GetUserUseCase {
    UserDetail obtenir(GetUserQuery query);
}
