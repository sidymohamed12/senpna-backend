package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AttribuerAppelOffreCommand;

public interface AttribuerAppelOffreUseCase {
    AppelOffreDetail attribuer(AttribuerAppelOffreCommand command);
}
