package ministere.sante.senpna.fournisseur.application.facade;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.DeactivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurPage;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ListFournisseursQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.port.in.ActivateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.CreateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.DeactivateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.GetFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.ListFournisseursUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.UpdateFournisseurUseCase;

import org.springframework.stereotype.Component;

@Component
public class FournisseurFacade {

    private final CreateFournisseurUseCase createFournisseurUseCase;
    private final UpdateFournisseurUseCase updateFournisseurUseCase;
    private final GetFournisseurUseCase getFournisseurUseCase;
    private final ListFournisseursUseCase listFournisseursUseCase;
    private final ActivateFournisseurUseCase activateFournisseurUseCase;
    private final DeactivateFournisseurUseCase deactivateFournisseurUseCase;

    public FournisseurFacade(
            CreateFournisseurUseCase createFournisseurUseCase,
            UpdateFournisseurUseCase updateFournisseurUseCase,
            GetFournisseurUseCase getFournisseurUseCase,
            ListFournisseursUseCase listFournisseursUseCase,
            ActivateFournisseurUseCase activateFournisseurUseCase,
            DeactivateFournisseurUseCase deactivateFournisseurUseCase) {
        this.createFournisseurUseCase = createFournisseurUseCase;
        this.updateFournisseurUseCase = updateFournisseurUseCase;
        this.getFournisseurUseCase = getFournisseurUseCase;
        this.listFournisseursUseCase = listFournisseursUseCase;
        this.activateFournisseurUseCase = activateFournisseurUseCase;
        this.deactivateFournisseurUseCase = deactivateFournisseurUseCase;
    }

    public FournisseurDetail creerFournisseur(CreateFournisseurCommand command) {
        return createFournisseurUseCase.creer(command);
    }

    public FournisseurDetail modifierFournisseur(UpdateFournisseurCommand command) {
        return updateFournisseurUseCase.modifier(command);
    }

    public FournisseurDetail obtenirFournisseur(GetFournisseurQuery query) {
        return getFournisseurUseCase.obtenir(query);
    }

    public FournisseurPage listerFournisseurs(ListFournisseursQuery query) {
        return listFournisseursUseCase.lister(query);
    }

    public FournisseurDetail activerFournisseur(ActivateFournisseurCommand command) {
        return activateFournisseurUseCase.activer(command);
    }

    public FournisseurDetail desactiverFournisseur(DeactivateFournisseurCommand command) {
        return deactivateFournisseurUseCase.desactiver(command);
    }
}
