package ministere.sante.senpna.fournisseur.domain.port.in;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;

public interface ActivateFournisseurUseCase {
    FournisseurDetail activer(ActivateFournisseurCommand command);
}
