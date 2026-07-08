package ministere.sante.senpna.carriere.application.facade;

import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidaturePage;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.GetCandidatureQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.ListCandidaturesQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.SoumettreCandidatureCommand;
import ministere.sante.senpna.carriere.domain.port.in.GetCandidatureUseCase;
import ministere.sante.senpna.carriere.domain.port.in.ListCandidaturesUseCase;
import ministere.sante.senpna.carriere.domain.port.in.SoumettreCandidatureUseCase;

import org.springframework.stereotype.Component;

@Component
public class CandidatureFacade {

    private final SoumettreCandidatureUseCase soumettreCandidatureUseCase;
    private final GetCandidatureUseCase getCandidatureUseCase;
    private final ListCandidaturesUseCase listCandidaturesUseCase;

    public CandidatureFacade(
            SoumettreCandidatureUseCase soumettreCandidatureUseCase,
            GetCandidatureUseCase getCandidatureUseCase,
            ListCandidaturesUseCase listCandidaturesUseCase) {
        this.soumettreCandidatureUseCase = soumettreCandidatureUseCase;
        this.getCandidatureUseCase = getCandidatureUseCase;
        this.listCandidaturesUseCase = listCandidaturesUseCase;
    }

    public CandidatureDetail soumettreCandidature(SoumettreCandidatureCommand command) {
        return soumettreCandidatureUseCase.soumettre(command);
    }

    public CandidatureDetail obtenirCandidature(GetCandidatureQuery query) {
        return getCandidatureUseCase.obtenir(query);
    }

    public CandidaturePage listerCandidatures(ListCandidaturesQuery query) {
        return listCandidaturesUseCase.lister(query);
    }
}
