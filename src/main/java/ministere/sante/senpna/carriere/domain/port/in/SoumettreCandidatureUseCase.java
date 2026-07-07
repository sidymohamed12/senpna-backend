package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.SoumettreCandidatureCommand;

public interface SoumettreCandidatureUseCase {
    CandidatureDetail soumettre(SoumettreCandidatureCommand command);
}
