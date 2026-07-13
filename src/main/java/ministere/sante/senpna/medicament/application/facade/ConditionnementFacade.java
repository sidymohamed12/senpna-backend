package ministere.sante.senpna.medicament.application.facade;

import org.springframework.stereotype.Component;

import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.CreateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.DesarchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.GetConditionnementQuery;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ListConditionnementsQuery;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.UpdateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.ArchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.CreateConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.DesarchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.GetConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.ListConditionnementsUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.UpdateConditionnementUseCase;

@Component
public class ConditionnementFacade {

    private final CreateConditionnementUseCase createConditionnementUseCase;
    private final UpdateConditionnementUseCase updateConditionnementUseCase;
    private final ArchiveConditionnementUseCase archiveConditionnementUseCase;
    private final DesarchiveConditionnementUseCase desarchiveConditionnementUseCase;
    private final GetConditionnementUseCase getConditionnementUseCase;
    private final ListConditionnementsUseCase listConditionnementsUseCase;

    public ConditionnementFacade(CreateConditionnementUseCase createConditionnementUseCase,
            UpdateConditionnementUseCase updateConditionnementUseCase,
            ArchiveConditionnementUseCase archiveConditionnementUseCase,
            DesarchiveConditionnementUseCase desarchiveConditionnementUseCase,
            GetConditionnementUseCase getConditionnementUseCase,
            ListConditionnementsUseCase listConditionnementsUseCase) {
        this.createConditionnementUseCase = createConditionnementUseCase;
        this.updateConditionnementUseCase = updateConditionnementUseCase;
        this.archiveConditionnementUseCase = archiveConditionnementUseCase;
        this.desarchiveConditionnementUseCase = desarchiveConditionnementUseCase;
        this.getConditionnementUseCase = getConditionnementUseCase;
        this.listConditionnementsUseCase = listConditionnementsUseCase;
    }

    public ConditionnementDetail creerConditionnement(CreateConditionnementCommand command) {
        return createConditionnementUseCase.creer(command);
    }

    public ConditionnementDetail modifierConditionnement(UpdateConditionnementCommand command) {
        return updateConditionnementUseCase.modifier(command);
    }

    public ConditionnementDetail archiverConditionnement(ArchiveConditionnementCommand command) {
        return archiveConditionnementUseCase.archiver(command);
    }

    public ConditionnementDetail desarchiverConditionnement(DesarchiveConditionnementCommand command) {
        return desarchiveConditionnementUseCase.desarchiver(command);
    }

    public ConditionnementDetail obtenirConditionnement(GetConditionnementQuery query) {
        return getConditionnementUseCase.obtenir(query);
    }

    public ConditionnementPage listerConditionnements(ListConditionnementsQuery query) {
        return listConditionnementsUseCase.lister(query);
    }
}
