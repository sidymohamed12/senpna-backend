package ministere.sante.senpna.medicament.domain.port.in.conditionnement;

import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.UpdateConditionnementCommand;

public interface UpdateConditionnementUseCase {
    ConditionnementDetail modifier(UpdateConditionnementCommand command);
}
