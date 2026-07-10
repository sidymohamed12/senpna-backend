package ministere.sante.senpna.medicament.domain.port.in.conditionnement;

import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ListConditionnementsQuery;

public interface ListConditionnementsUseCase {
    ConditionnementPage lister(ListConditionnementsQuery query);
}
