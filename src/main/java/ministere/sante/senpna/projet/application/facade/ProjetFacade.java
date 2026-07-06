package ministere.sante.senpna.projet.application.facade;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ArchiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.CreateProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.DesactiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.GetProjetQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ListProjetsQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetPage;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.PublierProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.RemettreEnBrouillonProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.UpdateProjetCommand;
import ministere.sante.senpna.projet.domain.port.in.ArchiverProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.CreateProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.DesactiverProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.GetProjetPubliqueUseCase;
import ministere.sante.senpna.projet.domain.port.in.GetProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.ListProjetsUseCase;
import ministere.sante.senpna.projet.domain.port.in.PublierProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.RemettreEnBrouillonProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.UpdateProjetUseCase;

import org.springframework.stereotype.Component;

@Component
public class ProjetFacade {

    private final CreateProjetUseCase createProjetUseCase;
    private final UpdateProjetUseCase updateProjetUseCase;
    private final PublierProjetUseCase publierProjetUseCase;
    private final ArchiverProjetUseCase archiverProjetUseCase;
    private final DesactiverProjetUseCase desactiverProjetUseCase;
    private final RemettreEnBrouillonProjetUseCase remettreEnBrouillonProjetUseCase;
    private final GetProjetUseCase getProjetUseCase;
    private final GetProjetPubliqueUseCase getProjetPubliqueUseCase;
    private final ListProjetsUseCase listProjetsUseCase;

    public ProjetFacade(
            CreateProjetUseCase createProjetUseCase,
            UpdateProjetUseCase updateProjetUseCase,
            PublierProjetUseCase publierProjetUseCase,
            ArchiverProjetUseCase archiverProjetUseCase,
            DesactiverProjetUseCase desactiverProjetUseCase,
            RemettreEnBrouillonProjetUseCase remettreEnBrouillonProjetUseCase,
            GetProjetUseCase getProjetUseCase,
            GetProjetPubliqueUseCase getProjetPubliqueUseCase,
            ListProjetsUseCase listProjetsUseCase) {
        this.createProjetUseCase = createProjetUseCase;
        this.updateProjetUseCase = updateProjetUseCase;
        this.publierProjetUseCase = publierProjetUseCase;
        this.archiverProjetUseCase = archiverProjetUseCase;
        this.desactiverProjetUseCase = desactiverProjetUseCase;
        this.remettreEnBrouillonProjetUseCase = remettreEnBrouillonProjetUseCase;
        this.getProjetUseCase = getProjetUseCase;
        this.getProjetPubliqueUseCase = getProjetPubliqueUseCase;
        this.listProjetsUseCase = listProjetsUseCase;
    }

    public ProjetDetail creerProjet(CreateProjetCommand command) {
        return createProjetUseCase.creer(command);
    }

    public ProjetDetail modifierProjet(UpdateProjetCommand command) {
        return updateProjetUseCase.modifier(command);
    }

    public ProjetDetail publierProjet(PublierProjetCommand command) {
        return publierProjetUseCase.publier(command);
    }

    public ProjetDetail archiverProjet(ArchiverProjetCommand command) {
        return archiverProjetUseCase.archiver(command);
    }

    public ProjetDetail desactiverProjet(DesactiverProjetCommand command) {
        return desactiverProjetUseCase.desactiver(command);
    }

    public ProjetDetail remettreEnBrouillonProjet(RemettreEnBrouillonProjetCommand command) {
        return remettreEnBrouillonProjetUseCase.remettreEnBrouillon(command);
    }

    public ProjetDetail obtenirProjet(GetProjetQuery query) {
        return getProjetUseCase.obtenir(query);
    }

    public ProjetDetail obtenirProjetPublique(GetProjetQuery query) {
        return getProjetPubliqueUseCase.obtenirPublique(query);
    }

    public ProjetPage listerProjets(ListProjetsQuery query) {
        return listProjetsUseCase.lister(query);
    }
}
