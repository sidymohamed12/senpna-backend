package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AnnulerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;

public interface AnnulerAppelOffreUseCase {
    AppelOffreDetail annuler(AnnulerAppelOffreCommand command);
}
