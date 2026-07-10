package ministere.sante.senpna.organisation.application.facade;

import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.Entrepot.GetEntrepotQuery;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ListEntrepotsQuery;
import ministere.sante.senpna.organisation.domain.port.in.entrepot.GetEntrepotUseCase;
import ministere.sante.senpna.organisation.domain.port.in.entrepot.ListEntrepotsUseCase;

public class EntrepotFacade {

    private final GetEntrepotUseCase getEntrepotUseCase;
    private final ListEntrepotsUseCase listEntrepotsUseCase;

    public EntrepotFacade(GetEntrepotUseCase getEntrepotUseCase, ListEntrepotsUseCase listEntrepotsUseCase) {
        this.getEntrepotUseCase = getEntrepotUseCase;
        this.listEntrepotsUseCase = listEntrepotsUseCase;
    }

    public EntrepotDetail obtenirEntrepot(GetEntrepotQuery query) {
        return getEntrepotUseCase.obtenir(query);
    }

    public EntrepotPage listerEntrepots(ListEntrepotsQuery query) {
        return listEntrepotsUseCase.lister(query);
    }

}
