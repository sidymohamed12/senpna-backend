package ministere.sante.senpna.actualite.application.facade;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualitePage;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.CreateActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.DesactiverActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.GetActualiteQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ListActualitesQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.PublierActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.RemettreEnBrouillonActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.UpdateActualiteCommand;
import ministere.sante.senpna.actualite.domain.port.in.CreateActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.DesactiverActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.GetActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.ListActualitesUseCase;
import ministere.sante.senpna.actualite.domain.port.in.PublierActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.RemettreEnBrouillonActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.UpdateActualiteUseCase;

import org.springframework.stereotype.Component;

@Component
public class ActualiteFacade {

    private final CreateActualiteUseCase createActualiteUseCase;
    private final UpdateActualiteUseCase updateActualiteUseCase;
    private final PublierActualiteUseCase publierActualiteUseCase;
    private final DesactiverActualiteUseCase desactiverActualiteUseCase;
    private final RemettreEnBrouillonActualiteUseCase remettreEnBrouillonActualiteUseCase;
    private final GetActualiteUseCase getActualiteUseCase;
    private final ListActualitesUseCase listActualitesUseCase;

    public ActualiteFacade(
            CreateActualiteUseCase createActualiteUseCase,
            UpdateActualiteUseCase updateActualiteUseCase,
            PublierActualiteUseCase publierActualiteUseCase,
            DesactiverActualiteUseCase desactiverActualiteUseCase,
            RemettreEnBrouillonActualiteUseCase remettreEnBrouillonActualiteUseCase,
            GetActualiteUseCase getActualiteUseCase,
            ListActualitesUseCase listActualitesUseCase) {
        this.createActualiteUseCase = createActualiteUseCase;
        this.updateActualiteUseCase = updateActualiteUseCase;
        this.publierActualiteUseCase = publierActualiteUseCase;
        this.desactiverActualiteUseCase = desactiverActualiteUseCase;
        this.remettreEnBrouillonActualiteUseCase = remettreEnBrouillonActualiteUseCase;
        this.getActualiteUseCase = getActualiteUseCase;
        this.listActualitesUseCase = listActualitesUseCase;
    }

    public ActualiteDetail creerActualite(CreateActualiteCommand command) {
        return createActualiteUseCase.creer(command);
    }

    public ActualiteDetail modifierActualite(UpdateActualiteCommand command) {
        return updateActualiteUseCase.modifier(command);
    }

    public ActualiteDetail publierActualite(PublierActualiteCommand command) {
        return publierActualiteUseCase.publier(command);
    }

    public ActualiteDetail desactiverActualite(DesactiverActualiteCommand command) {
        return desactiverActualiteUseCase.desactiver(command);
    }

    public ActualiteDetail remettreEnBrouillonActualite(RemettreEnBrouillonActualiteCommand command) {
        return remettreEnBrouillonActualiteUseCase.remettreEnBrouillon(command);
    }

    public ActualiteDetail obtenirActualite(GetActualiteQuery query) {
        return getActualiteUseCase.obtenir(query);
    }

    public ActualitePage listerActualites(ListActualitesQuery query) {
        return listActualitesUseCase.lister(query);
    }
}
