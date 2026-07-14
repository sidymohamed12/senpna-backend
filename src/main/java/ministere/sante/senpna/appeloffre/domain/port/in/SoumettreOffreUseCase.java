package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.SoumettreOffreCommand;

public interface SoumettreOffreUseCase {
    OffreDetail soumettre(SoumettreOffreCommand command);
}
