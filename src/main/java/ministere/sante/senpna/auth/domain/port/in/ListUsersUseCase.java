package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.ListUsersQuery;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserPage;

public interface ListUsersUseCase {
    UserPage lister(ListUsersQuery query);
}
