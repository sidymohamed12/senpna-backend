package ministere.sante.senpna.organisation.domain.port.in.pra;

import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.UpdatePraCommand;

public interface UpdatePraUseCase {
    EntrepotDetail modifier(UpdatePraCommand command);
}
