package ministere.sante.senpna.medicament.application.facade;

import org.springframework.stereotype.Component;

import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.DesarchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.GetFamilleQuery;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ListFamillesQuery;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.UpdateFamilleCommand;
import ministere.sante.senpna.medicament.domain.port.in.famille.ArchiveFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.CreateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.DesarchiveFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.GetFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.ListFamillesUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.UpdateFamilleUseCase;

@Component
public class FamilleFacade {

    private final CreateFamilleUseCase createFamilleUseCase;
    private final UpdateFamilleUseCase updateFamilleUseCase;
    private final ArchiveFamilleUseCase archiveFamilleUseCase;
    private final DesarchiveFamilleUseCase desarchiveFamilleUseCase;
    private final GetFamilleUseCase getFamilleUseCase;
    private final ListFamillesUseCase listFamillesUseCase;

    public FamilleFacade(CreateFamilleUseCase createFamilleUseCase, UpdateFamilleUseCase updateFamilleUseCase,
            ArchiveFamilleUseCase archiveFamilleUseCase, DesarchiveFamilleUseCase desarchiveFamilleUseCase,
            GetFamilleUseCase getFamilleUseCase, ListFamillesUseCase listFamillesUseCase) {
        this.createFamilleUseCase = createFamilleUseCase;
        this.updateFamilleUseCase = updateFamilleUseCase;
        this.archiveFamilleUseCase = archiveFamilleUseCase;
        this.desarchiveFamilleUseCase = desarchiveFamilleUseCase;
        this.getFamilleUseCase = getFamilleUseCase;
        this.listFamillesUseCase = listFamillesUseCase;
    }

    public FamilleDetail creerFamille(CreateFamilleCommand command) {
        return createFamilleUseCase.creer(command);
    }

    public FamilleDetail modifierFamille(UpdateFamilleCommand command) {
        return updateFamilleUseCase.modifier(command);
    }

    public FamilleDetail archiverFamille(ArchiveFamilleCommand command) {
        return archiveFamilleUseCase.archiver(command);
    }

    public FamilleDetail desarchiverFamille(DesarchiveFamilleCommand command) {
        return desarchiveFamilleUseCase.desarchiver(command);
    }

    public FamilleDetail obtenirFamille(GetFamilleQuery query) {
        return getFamilleUseCase.obtenir(query);
    }

    public FamillePage listerFamilles(ListFamillesQuery query) {
        return listFamillesUseCase.lister(query);
    }

}
