package ministere.sante.senpna.carriere.application.facade;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CloturerOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CreateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.ListOpportunitesCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.MettreEnCoursOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarrierePage;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.PublierOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.RemettreEnBrouillonOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.UpdateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.port.in.CloturerOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.CreateOpportuniteCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.in.GetOpportuniteCarrierePubliqueUseCase;
import ministere.sante.senpna.carriere.domain.port.in.GetOpportuniteCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.in.ListOpportunitesCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.in.MettreEnCoursOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.PublierOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.RemettreEnBrouillonOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.UpdateOpportuniteCarriereUseCase;

import org.springframework.stereotype.Component;

@Component
public class OpportuniteCarriereFacade {

    private final CreateOpportuniteCarriereUseCase createOpportuniteCarriereUseCase;
    private final UpdateOpportuniteCarriereUseCase updateOpportuniteCarriereUseCase;
    private final PublierOpportuniteUseCase publierOpportuniteUseCase;
    private final MettreEnCoursOpportuniteUseCase mettreEnCoursOpportuniteUseCase;
    private final CloturerOpportuniteUseCase cloturerOpportuniteUseCase;
    private final RemettreEnBrouillonOpportuniteUseCase remettreEnBrouillonOpportuniteUseCase;
    private final GetOpportuniteCarriereUseCase getOpportuniteCarriereUseCase;
    private final GetOpportuniteCarrierePubliqueUseCase getOpportuniteCarrierePubliqueUseCase;
    private final ListOpportunitesCarriereUseCase listOpportunitesCarriereUseCase;

    public OpportuniteCarriereFacade(
            CreateOpportuniteCarriereUseCase createOpportuniteCarriereUseCase,
            UpdateOpportuniteCarriereUseCase updateOpportuniteCarriereUseCase,
            PublierOpportuniteUseCase publierOpportuniteUseCase,
            MettreEnCoursOpportuniteUseCase mettreEnCoursOpportuniteUseCase,
            CloturerOpportuniteUseCase cloturerOpportuniteUseCase,
            RemettreEnBrouillonOpportuniteUseCase remettreEnBrouillonOpportuniteUseCase,
            GetOpportuniteCarriereUseCase getOpportuniteCarriereUseCase,
            GetOpportuniteCarrierePubliqueUseCase getOpportuniteCarrierePubliqueUseCase,
            ListOpportunitesCarriereUseCase listOpportunitesCarriereUseCase) {
        this.createOpportuniteCarriereUseCase = createOpportuniteCarriereUseCase;
        this.updateOpportuniteCarriereUseCase = updateOpportuniteCarriereUseCase;
        this.publierOpportuniteUseCase = publierOpportuniteUseCase;
        this.mettreEnCoursOpportuniteUseCase = mettreEnCoursOpportuniteUseCase;
        this.cloturerOpportuniteUseCase = cloturerOpportuniteUseCase;
        this.remettreEnBrouillonOpportuniteUseCase = remettreEnBrouillonOpportuniteUseCase;
        this.getOpportuniteCarriereUseCase = getOpportuniteCarriereUseCase;
        this.getOpportuniteCarrierePubliqueUseCase = getOpportuniteCarrierePubliqueUseCase;
        this.listOpportunitesCarriereUseCase = listOpportunitesCarriereUseCase;
    }

    public OpportuniteCarriereDetail creerOpportunite(CreateOpportuniteCarriereCommand command) {
        return createOpportuniteCarriereUseCase.creer(command);
    }

    public OpportuniteCarriereDetail modifierOpportunite(UpdateOpportuniteCarriereCommand command) {
        return updateOpportuniteCarriereUseCase.modifier(command);
    }

    public OpportuniteCarriereDetail publierOpportunite(PublierOpportuniteCommand command) {
        return publierOpportuniteUseCase.publier(command);
    }

    public OpportuniteCarriereDetail mettreEnCoursOpportunite(MettreEnCoursOpportuniteCommand command) {
        return mettreEnCoursOpportuniteUseCase.mettreEnCours(command);
    }

    public OpportuniteCarriereDetail cloturerOpportunite(CloturerOpportuniteCommand command) {
        return cloturerOpportuniteUseCase.cloturer(command);
    }

    public OpportuniteCarriereDetail remettreEnBrouillonOpportunite(RemettreEnBrouillonOpportuniteCommand command) {
        return remettreEnBrouillonOpportuniteUseCase.remettreEnBrouillon(command);
    }

    public OpportuniteCarriereDetail obtenirOpportunite(GetOpportuniteCarriereQuery query) {
        return getOpportuniteCarriereUseCase.obtenir(query);
    }

    public OpportuniteCarriereDetail obtenirOpportunitePublique(GetOpportuniteCarriereQuery query) {
        return getOpportuniteCarrierePubliqueUseCase.obtenirPublique(query);
    }

    public OpportuniteCarrierePage listerOpportunites(ListOpportunitesCarriereQuery query) {
        return listOpportunitesCarriereUseCase.lister(query);
    }
}
