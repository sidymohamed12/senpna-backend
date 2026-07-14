package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;

public interface ListAppelOffresUseCase {
    AppelOffrePage lister(ListAppelOffresQuery query);
}
