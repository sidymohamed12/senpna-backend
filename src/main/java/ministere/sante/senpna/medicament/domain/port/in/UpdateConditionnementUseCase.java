package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateConditionnementCommand;

public interface UpdateConditionnementUseCase {
    ConditionnementDetail modifier(UpdateConditionnementCommand command);
}
