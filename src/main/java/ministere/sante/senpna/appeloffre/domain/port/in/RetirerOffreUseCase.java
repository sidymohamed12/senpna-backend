package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetirerOffreCommand;

public interface RetirerOffreUseCase {
    OffreDetail retirer(RetirerOffreCommand command);
}
