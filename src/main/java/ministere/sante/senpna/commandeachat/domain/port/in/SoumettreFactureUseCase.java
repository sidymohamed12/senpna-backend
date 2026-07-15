package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.SoumettreFactureCommand;

public interface SoumettreFactureUseCase {
    FactureDetail soumettre(SoumettreFactureCommand command);
}
