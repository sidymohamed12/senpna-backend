package ministere.sante.senpna.organisation.domain.port.in.pra;

import ministere.sante.senpna.organisation.domain.command.Entrepot.DeactivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;

public interface DeactivatePraUseCase {
    EntrepotDetail desactiver(DeactivatePraCommand command);
}
