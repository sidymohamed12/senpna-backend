package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.PublierAppelOffreCommand;

public interface PublierAppelOffreUseCase {
    AppelOffreDetail publier(PublierAppelOffreCommand command);
}
