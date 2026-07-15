package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GenererAvisExpeditionCommand;

/** Fournisseur — génération de l'avis d'expédition (suivi des livraisons). */
public interface GenererAvisExpeditionUseCase {
    CommandeAchatDetail generer(GenererAvisExpeditionCommand command);
}
