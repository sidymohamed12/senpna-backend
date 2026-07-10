package ministere.sante.senpna.organisation.domain.port.in.pra;

import ministere.sante.senpna.organisation.domain.command.Entrepot.CreatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;

public interface CreatePraUseCase {
    EntrepotDetail creer(CreatePraCommand command);
}
