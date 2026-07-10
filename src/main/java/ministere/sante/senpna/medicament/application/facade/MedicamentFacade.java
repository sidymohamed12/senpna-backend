package ministere.sante.senpna.medicament.application.facade;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.port.in.medicament.ArchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.CreateMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.DesarchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.GetMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.ListMedicamentsUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.UpdateMedicamentUseCase;

import org.springframework.stereotype.Component;

@Component
public class MedicamentFacade {

    private final CreateMedicamentUseCase createMedicamentUseCase;
    private final UpdateMedicamentUseCase updateMedicamentUseCase;
    private final ArchiveMedicamentUseCase archiveMedicamentUseCase;
    private final DesarchiveMedicamentUseCase desarchiveMedicamentUseCase;
    private final GetMedicamentUseCase getMedicamentUseCase;
    private final ListMedicamentsUseCase listMedicamentsUseCase;

    public MedicamentFacade(
            CreateMedicamentUseCase createMedicamentUseCase,
            UpdateMedicamentUseCase updateMedicamentUseCase,
            ArchiveMedicamentUseCase archiveMedicamentUseCase,
            DesarchiveMedicamentUseCase desarchiveMedicamentUseCase,
            GetMedicamentUseCase getMedicamentUseCase,
            ListMedicamentsUseCase listMedicamentsUseCase) {

        this.createMedicamentUseCase = createMedicamentUseCase;
        this.updateMedicamentUseCase = updateMedicamentUseCase;
        this.archiveMedicamentUseCase = archiveMedicamentUseCase;
        this.desarchiveMedicamentUseCase = desarchiveMedicamentUseCase;
        this.getMedicamentUseCase = getMedicamentUseCase;
        this.listMedicamentsUseCase = listMedicamentsUseCase;
    }

    // ── Famille ─────────────────────────────────────────────────────────

    // ── Forme ───────────────────────────────────────────────────────────

    // ── Médicament ──────────────────────────────────────────────────────

    public MedicamentDetail creerMedicament(CreateMedicamentCommand command) {
        return createMedicamentUseCase.creer(command);
    }

    public MedicamentDetail modifierMedicament(UpdateMedicamentCommand command) {
        return updateMedicamentUseCase.modifier(command);
    }

    public MedicamentDetail archiverMedicament(ArchiveMedicamentCommand command) {
        return archiveMedicamentUseCase.archiver(command);
    }

    public MedicamentDetail desarchiverMedicament(DesarchiveMedicamentCommand command) {
        return desarchiveMedicamentUseCase.desarchiver(command);
    }

    public MedicamentDetail obtenirMedicament(GetMedicamentQuery query) {
        return getMedicamentUseCase.obtenir(query);
    }

    public MedicamentPage listerMedicaments(ListMedicamentsQuery query) {
        return listMedicamentsUseCase.lister(query);
    }

    // ── Conditionnement ─────────────────────────────────────────────────

}
