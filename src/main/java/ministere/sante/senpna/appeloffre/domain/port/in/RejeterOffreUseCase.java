package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RejeterOffreCommand;

public interface RejeterOffreUseCase {
    OffreDetail rejeter(RejeterOffreCommand command);
}
