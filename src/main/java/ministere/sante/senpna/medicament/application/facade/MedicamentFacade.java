package ministere.sante.senpna.medicament.application.facade;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetConditionnementQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetFamilleQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetFormeQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListConditionnementsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFamillesQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFormesQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.port.in.ArchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.ArchiveFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.ArchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.ArchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.CreateConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.CreateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.CreateFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.CreateMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.DesarchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.DesarchiveFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.DesarchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.DesarchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.GetConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.GetFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.GetFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.GetMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.ListConditionnementsUseCase;
import ministere.sante.senpna.medicament.domain.port.in.ListFamillesUseCase;
import ministere.sante.senpna.medicament.domain.port.in.ListFormesUseCase;
import ministere.sante.senpna.medicament.domain.port.in.ListMedicamentsUseCase;
import ministere.sante.senpna.medicament.domain.port.in.UpdateConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.UpdateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.UpdateFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.UpdateMedicamentUseCase;

import org.springframework.stereotype.Component;

@Component
public class MedicamentFacade {

    private final CreateFamilleUseCase createFamilleUseCase;
    private final UpdateFamilleUseCase updateFamilleUseCase;
    private final ArchiveFamilleUseCase archiveFamilleUseCase;
    private final DesarchiveFamilleUseCase desarchiveFamilleUseCase;
    private final GetFamilleUseCase getFamilleUseCase;
    private final ListFamillesUseCase listFamillesUseCase;

    private final CreateFormeUseCase createFormeUseCase;
    private final UpdateFormeUseCase updateFormeUseCase;
    private final ArchiveFormeUseCase archiveFormeUseCase;
    private final DesarchiveFormeUseCase desarchiveFormeUseCase;
    private final GetFormeUseCase getFormeUseCase;
    private final ListFormesUseCase listFormesUseCase;

    private final CreateMedicamentUseCase createMedicamentUseCase;
    private final UpdateMedicamentUseCase updateMedicamentUseCase;
    private final ArchiveMedicamentUseCase archiveMedicamentUseCase;
    private final DesarchiveMedicamentUseCase desarchiveMedicamentUseCase;
    private final GetMedicamentUseCase getMedicamentUseCase;
    private final ListMedicamentsUseCase listMedicamentsUseCase;

    private final CreateConditionnementUseCase createConditionnementUseCase;
    private final UpdateConditionnementUseCase updateConditionnementUseCase;
    private final ArchiveConditionnementUseCase archiveConditionnementUseCase;
    private final DesarchiveConditionnementUseCase desarchiveConditionnementUseCase;
    private final GetConditionnementUseCase getConditionnementUseCase;
    private final ListConditionnementsUseCase listConditionnementsUseCase;

    public MedicamentFacade(
            CreateFamilleUseCase createFamilleUseCase,
            UpdateFamilleUseCase updateFamilleUseCase,
            ArchiveFamilleUseCase archiveFamilleUseCase,
            DesarchiveFamilleUseCase desarchiveFamilleUseCase,
            GetFamilleUseCase getFamilleUseCase,
            ListFamillesUseCase listFamillesUseCase,
            CreateFormeUseCase createFormeUseCase,
            UpdateFormeUseCase updateFormeUseCase,
            ArchiveFormeUseCase archiveFormeUseCase,
            DesarchiveFormeUseCase desarchiveFormeUseCase,
            GetFormeUseCase getFormeUseCase,
            ListFormesUseCase listFormesUseCase,
            CreateMedicamentUseCase createMedicamentUseCase,
            UpdateMedicamentUseCase updateMedicamentUseCase,
            ArchiveMedicamentUseCase archiveMedicamentUseCase,
            DesarchiveMedicamentUseCase desarchiveMedicamentUseCase,
            GetMedicamentUseCase getMedicamentUseCase,
            ListMedicamentsUseCase listMedicamentsUseCase,
            CreateConditionnementUseCase createConditionnementUseCase,
            UpdateConditionnementUseCase updateConditionnementUseCase,
            ArchiveConditionnementUseCase archiveConditionnementUseCase,
            DesarchiveConditionnementUseCase desarchiveConditionnementUseCase,
            GetConditionnementUseCase getConditionnementUseCase,
            ListConditionnementsUseCase listConditionnementsUseCase) {
        this.createFamilleUseCase = createFamilleUseCase;
        this.updateFamilleUseCase = updateFamilleUseCase;
        this.archiveFamilleUseCase = archiveFamilleUseCase;
        this.desarchiveFamilleUseCase = desarchiveFamilleUseCase;
        this.getFamilleUseCase = getFamilleUseCase;
        this.listFamillesUseCase = listFamillesUseCase;
        this.createFormeUseCase = createFormeUseCase;
        this.updateFormeUseCase = updateFormeUseCase;
        this.archiveFormeUseCase = archiveFormeUseCase;
        this.desarchiveFormeUseCase = desarchiveFormeUseCase;
        this.getFormeUseCase = getFormeUseCase;
        this.listFormesUseCase = listFormesUseCase;
        this.createMedicamentUseCase = createMedicamentUseCase;
        this.updateMedicamentUseCase = updateMedicamentUseCase;
        this.archiveMedicamentUseCase = archiveMedicamentUseCase;
        this.desarchiveMedicamentUseCase = desarchiveMedicamentUseCase;
        this.getMedicamentUseCase = getMedicamentUseCase;
        this.listMedicamentsUseCase = listMedicamentsUseCase;
        this.createConditionnementUseCase = createConditionnementUseCase;
        this.updateConditionnementUseCase = updateConditionnementUseCase;
        this.archiveConditionnementUseCase = archiveConditionnementUseCase;
        this.desarchiveConditionnementUseCase = desarchiveConditionnementUseCase;
        this.getConditionnementUseCase = getConditionnementUseCase;
        this.listConditionnementsUseCase = listConditionnementsUseCase;
    }

    // ── Famille ─────────────────────────────────────────────────────────

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

    // ── Forme ───────────────────────────────────────────────────────────

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
