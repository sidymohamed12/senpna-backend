package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListConditionnementsQuery;

public interface ListConditionnementsUseCase {
    ConditionnementPage lister(ListConditionnementsQuery query);
}
