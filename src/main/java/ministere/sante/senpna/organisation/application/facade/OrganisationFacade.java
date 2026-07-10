package ministere.sante.senpna.organisation.application.facade;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.*;
import ministere.sante.senpna.organisation.domain.port.in.affectation.AssignStructureToPraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.affectation.AssignStructureToRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.affectation.AssignUserToEntrepotUseCase;
import ministere.sante.senpna.organisation.domain.port.in.affectation.AssignUserToStructureUseCase;
import ministere.sante.senpna.organisation.domain.port.in.affectation.RejectAdhesionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.affectation.UnassignUserUseCase;
import ministere.sante.senpna.organisation.domain.port.in.affectation.ValidateAdhesionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.structure.ActivateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.structure.CreateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.structure.DeactivateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.structure.GetStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.structure.ListStructuresSanitairesUseCase;
import ministere.sante.senpna.organisation.domain.port.in.structure.UpdateStructureSanitaireUseCase;

import org.springframework.stereotype.Component;

@Component
public class OrganisationFacade {

    private final CreateStructureSanitaireUseCase createStructureSanitaireUseCase;
    private final UpdateStructureSanitaireUseCase updateStructureSanitaireUseCase;
    private final ValidateAdhesionUseCase validateAdhesionUseCase;
    private final RejectAdhesionUseCase rejectAdhesionUseCase;
    private final ActivateStructureSanitaireUseCase activateStructureSanitaireUseCase;
    private final DeactivateStructureSanitaireUseCase deactivateStructureSanitaireUseCase;
    private final AssignStructureToRegionUseCase assignStructureToRegionUseCase;
    private final AssignStructureToPraUseCase assignStructureToPraUseCase;
    private final GetStructureSanitaireUseCase getStructureSanitaireUseCase;
    private final ListStructuresSanitairesUseCase listStructuresSanitairesUseCase;

    private final AssignUserToEntrepotUseCase assignUserToEntrepotUseCase;
    private final AssignUserToStructureUseCase assignUserToStructureUseCase;
    private final UnassignUserUseCase unassignUserUseCase;

    public OrganisationFacade(
            CreateStructureSanitaireUseCase createStructureSanitaireUseCase,
            UpdateStructureSanitaireUseCase updateStructureSanitaireUseCase,
            ValidateAdhesionUseCase validateAdhesionUseCase,
            RejectAdhesionUseCase rejectAdhesionUseCase,
            ActivateStructureSanitaireUseCase activateStructureSanitaireUseCase,
            DeactivateStructureSanitaireUseCase deactivateStructureSanitaireUseCase,
            AssignStructureToRegionUseCase assignStructureToRegionUseCase,
            AssignStructureToPraUseCase assignStructureToPraUseCase,
            GetStructureSanitaireUseCase getStructureSanitaireUseCase,
            ListStructuresSanitairesUseCase listStructuresSanitairesUseCase,
            AssignUserToEntrepotUseCase assignUserToEntrepotUseCase,
            AssignUserToStructureUseCase assignUserToStructureUseCase,
            UnassignUserUseCase unassignUserUseCase) {
        this.createStructureSanitaireUseCase = createStructureSanitaireUseCase;
        this.updateStructureSanitaireUseCase = updateStructureSanitaireUseCase;
        this.validateAdhesionUseCase = validateAdhesionUseCase;
        this.rejectAdhesionUseCase = rejectAdhesionUseCase;
        this.activateStructureSanitaireUseCase = activateStructureSanitaireUseCase;
        this.deactivateStructureSanitaireUseCase = deactivateStructureSanitaireUseCase;
        this.assignStructureToRegionUseCase = assignStructureToRegionUseCase;
        this.assignStructureToPraUseCase = assignStructureToPraUseCase;
        this.getStructureSanitaireUseCase = getStructureSanitaireUseCase;
        this.listStructuresSanitairesUseCase = listStructuresSanitairesUseCase;
        this.assignUserToEntrepotUseCase = assignUserToEntrepotUseCase;
        this.assignUserToStructureUseCase = assignUserToStructureUseCase;
        this.unassignUserUseCase = unassignUserUseCase;
    }

    // ── Structure sanitaire ─────────────────────────────────────────────

    public StructureSanitaireDetail creerStructureSanitaire(CreateStructureSanitaireCommand command) {
        return createStructureSanitaireUseCase.creer(command);
    }

    public StructureSanitaireDetail modifierStructureSanitaire(UpdateStructureSanitaireCommand command) {
        return updateStructureSanitaireUseCase.modifier(command);
    }

    public StructureSanitaireDetail validerAdhesion(ValidateAdhesionCommand command) {
        return validateAdhesionUseCase.valider(command);
    }

    public StructureSanitaireDetail rejeterAdhesion(RejectAdhesionCommand command) {
        return rejectAdhesionUseCase.rejeter(command);
    }

    public StructureSanitaireDetail activerStructureSanitaire(ActivateStructureSanitaireCommand command) {
        return activateStructureSanitaireUseCase.activer(command);
    }

    public StructureSanitaireDetail desactiverStructureSanitaire(DeactivateStructureSanitaireCommand command) {
        return deactivateStructureSanitaireUseCase.desactiver(command);
    }

    public StructureSanitaireDetail affecterStructureARegion(AssignStructureToRegionCommand command) {
        return assignStructureToRegionUseCase.affecter(command);
    }

    public StructureSanitaireDetail affecterStructureAPra(AssignStructureToPraCommand command) {
        return assignStructureToPraUseCase.affecter(command);
    }

    public StructureSanitaireDetail obtenirStructureSanitaire(GetStructureSanitaireQuery query) {
        return getStructureSanitaireUseCase.obtenir(query);
    }

    public StructureSanitairePage listerStructuresSanitaires(ListStructuresSanitairesQuery query) {
        return listStructuresSanitairesUseCase.lister(query);
    }

    // ── Affectation utilisateur ─────────────────────────────────────────

    public UserAffectationDetail affecterUtilisateurAEntrepot(AssignUserToEntrepotCommand command) {
        return assignUserToEntrepotUseCase.affecter(command);
    }

    public UserAffectationDetail affecterUtilisateurAStructure(AssignUserToStructureCommand command) {
        return assignUserToStructureUseCase.affecter(command);
    }

    public UserAffectationDetail retirerAffectationUtilisateur(UnassignUserCommand command) {
        return unassignUserUseCase.retirer(command);
    }
}
