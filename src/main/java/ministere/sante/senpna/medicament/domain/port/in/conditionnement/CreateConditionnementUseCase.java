package ministere.sante.senpna.medicament.domain.port.in.conditionnement;

import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.CreateConditionnementCommand;

public interface CreateConditionnementUseCase {
    ConditionnementDetail creer(CreateConditionnementCommand command);
}
