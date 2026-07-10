package ministere.sante.senpna.organisation.domain.port.in.pra;

import ministere.sante.senpna.organisation.domain.command.Entrepot.ActivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;

public interface ActivatePraUseCase {
    EntrepotDetail activer(ActivatePraCommand command);
}
