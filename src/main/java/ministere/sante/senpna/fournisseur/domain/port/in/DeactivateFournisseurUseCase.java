package ministere.sante.senpna.fournisseur.domain.port.in;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.DeactivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;

public interface DeactivateFournisseurUseCase {
    FournisseurDetail desactiver(DeactivateFournisseurCommand command);
}
