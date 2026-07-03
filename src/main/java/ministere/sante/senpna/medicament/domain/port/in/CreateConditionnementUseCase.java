package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateConditionnementCommand;

public interface CreateConditionnementUseCase {
    ConditionnementDetail creer(CreateConditionnementCommand command);
}
