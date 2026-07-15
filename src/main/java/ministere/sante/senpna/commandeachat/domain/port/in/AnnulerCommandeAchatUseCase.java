package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AnnulerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;

public interface AnnulerCommandeAchatUseCase {
    CommandeAchatDetail annuler(AnnulerCommandeAchatCommand command);
}
