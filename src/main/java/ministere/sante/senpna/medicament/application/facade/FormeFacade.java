package ministere.sante.senpna.medicament.application.facade;

import org.springframework.stereotype.Component;

import ministere.sante.senpna.medicament.domain.command.FormeCommands.ArchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.CreateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.DesarchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormePage;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.GetFormeQuery;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.ListFormesQuery;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.UpdateFormeCommand;
import ministere.sante.senpna.medicament.domain.port.in.forme.ArchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.CreateFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.DesarchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.GetFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.ListFormesUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.UpdateFormeUseCase;

@Component
public class FormeFacade {

    private final CreateFormeUseCase createFormeUseCase;
    private final UpdateFormeUseCase updateFormeUseCase;
    private final ArchiveFormeUseCase archiveFormeUseCase;
    private final DesarchiveFormeUseCase desarchiveFormeUseCase;
    private final GetFormeUseCase getFormeUseCase;
    private final ListFormesUseCase listFormesUseCase;

    public FormeFacade(CreateFormeUseCase createFormeUseCase, UpdateFormeUseCase updateFormeUseCase,
            ArchiveFormeUseCase archiveFormeUseCase, DesarchiveFormeUseCase desarchiveFormeUseCase,
            GetFormeUseCase getFormeUseCase, ListFormesUseCase listFormesUseCase) {
        this.createFormeUseCase = createFormeUseCase;
        this.updateFormeUseCase = updateFormeUseCase;
        this.archiveFormeUseCase = archiveFormeUseCase;
        this.desarchiveFormeUseCase = desarchiveFormeUseCase;
        this.getFormeUseCase = getFormeUseCase;
        this.listFormesUseCase = listFormesUseCase;
    }

    public FormeDetail creerForme(CreateFormeCommand command) {
        return createFormeUseCase.creer(command);
    }

    public FormeDetail modifierForme(UpdateFormeCommand command) {
        return updateFormeUseCase.modifier(command);
    }

    public FormeDetail archiverForme(ArchiveFormeCommand command) {
        return archiveFormeUseCase.archiver(command);
    }

    public FormeDetail desarchiverForme(DesarchiveFormeCommand command) {
        return desarchiveFormeUseCase.desarchiver(command);
    }

    public FormeDetail obtenirForme(GetFormeQuery query) {
        return getFormeUseCase.obtenir(query);
    }

    public FormePage listerFormes(ListFormesQuery query) {
        return listFormesUseCase.lister(query);
    }
}
