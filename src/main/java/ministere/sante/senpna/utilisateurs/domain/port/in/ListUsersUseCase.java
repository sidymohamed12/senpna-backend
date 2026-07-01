package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ListUsersQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserPage;

public interface ListUsersUseCase {
    UserPage lister(ListUsersQuery query);
}
