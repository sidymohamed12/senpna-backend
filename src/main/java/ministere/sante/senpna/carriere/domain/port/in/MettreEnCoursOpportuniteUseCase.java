package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.MettreEnCoursOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;

public interface MettreEnCoursOpportuniteUseCase {
    OpportuniteCarriereDetail mettreEnCours(MettreEnCoursOpportuniteCommand command);
}
