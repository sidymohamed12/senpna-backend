package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.CreateAppelOffreCommand;

public interface CreateAppelOffreUseCase {
    AppelOffreDetail creer(CreateAppelOffreCommand command);
}
