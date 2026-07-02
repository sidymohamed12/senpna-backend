package ministere.sante.senpna.fournisseur.domain.port.in;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;

public interface UpdateFournisseurUseCase {
    FournisseurDetail modifier(UpdateFournisseurCommand command);
}
