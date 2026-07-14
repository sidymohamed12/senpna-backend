package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ClorerAppelOffreCommand;

public interface ClorerAppelOffreUseCase {
    AppelOffreDetail clorer(ClorerAppelOffreCommand command);
}
