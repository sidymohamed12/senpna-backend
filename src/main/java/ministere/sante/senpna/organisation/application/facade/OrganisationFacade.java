package ministere.sante.senpna.organisation.application.facade;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.*;
import ministere.sante.senpna.organisation.domain.port.in.ActivatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.ActivateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.AssignStructureToPraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.AssignStructureToRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.AssignUserToEntrepotUseCase;
import ministere.sante.senpna.organisation.domain.port.in.AssignUserToStructureUseCase;
import ministere.sante.senpna.organisation.domain.port.in.CreatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.CreateRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.CreateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.DeactivatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.DeactivateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.GetEntrepotUseCase;
import ministere.sante.senpna.organisation.domain.port.in.GetRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.GetStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.ListEntrepotsUseCase;
import ministere.sante.senpna.organisation.domain.port.in.ListRegionsUseCase;
import ministere.sante.senpna.organisation.domain.port.in.ListStructuresSanitairesUseCase;
import ministere.sante.senpna.organisation.domain.port.in.RejectAdhesionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.UnassignUserUseCase;
import ministere.sante.senpna.organisation.domain.port.in.UpdatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.UpdateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.in.ValidateAdhesionUseCase;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganisationFacade {

    private final CreateRegionUseCase createRegionUseCase;
    private final GetRegionUseCase getRegionUseCase;
    private final ListRegionsUseCase listRegionsUseCase;

    private final CreatePraUseCase createPraUseCase;
    private final UpdatePraUseCase updatePraUseCase;
    private final DeactivatePraUseCase deactivatePraUseCase;
    private final ActivatePraUseCase activatePraUseCase;
    private final GetEntrepotUseCase getEntrepotUseCase;
    private final ListEntrepotsUseCase listEntrepotsUseCase;

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
            CreateRegionUseCase createRegionUseCase,
            GetRegionUseCase getRegionUseCase,
            ListRegionsUseCase listRegionsUseCase,
            CreatePraUseCase createPraUseCase,
            UpdatePraUseCase updatePraUseCase,
            DeactivatePraUseCase deactivatePraUseCase,
            ActivatePraUseCase activatePraUseCase,
            GetEntrepotUseCase getEntrepotUseCase,
            ListEntrepotsUseCase listEntrepotsUseCase,
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
        this.createRegionUseCase = createRegionUseCase;
        this.getRegionUseCase = getRegionUseCase;
        this.listRegionsUseCase = listRegionsUseCase;
        this.createPraUseCase = createPraUseCase;
        this.updatePraUseCase = updatePraUseCase;
        this.deactivatePraUseCase = deactivatePraUseCase;
        this.activatePraUseCase = activatePraUseCase;
        this.getEntrepotUseCase = getEntrepotUseCase;
        this.listEntrepotsUseCase = listEntrepotsUseCase;
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

    // ── Région ──────────────────────────────────────────────────────────

    public RegionDetail creerRegion(CreateRegionCommand command) {
        return createRegionUseCase.creer(command);
    }

    public RegionDetail obtenirRegion(GetRegionQuery query) {
        return getRegionUseCase.obtenir(query);
    }

    public List<RegionDetail> listerRegions() {
        return listRegionsUseCase.lister();
    }

    // ── PRA (entrepôt) ──────────────────────────────────────────────────

    public EntrepotDetail creerPra(CreatePraCommand command) {
        return createPraUseCase.creer(command);
    }

    public EntrepotDetail modifierPra(UpdatePraCommand command) {
        return updatePraUseCase.modifier(command);
    }

    public EntrepotDetail desactiverPra(DeactivatePraCommand command) {
        return deactivatePraUseCase.desactiver(command);
    }

    public EntrepotDetail activerPra(ActivatePraCommand command) {
        return activatePraUseCase.activer(command);
    }

    public EntrepotDetail obtenirEntrepot(GetEntrepotQuery query) {
        return getEntrepotUseCase.obtenir(query);
    }

    public EntrepotPage listerEntrepots(ListEntrepotsQuery query) {
        return listEntrepotsUseCase.lister(query);
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
