package ministere.sante.senpna.organisation.application.facade;

import ministere.sante.senpna.organisation.domain.command.Entrepot.ActivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.CreatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.DeactivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.UpdatePraCommand;
import ministere.sante.senpna.organisation.domain.port.in.pra.ActivatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.pra.CreatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.pra.DeactivatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.in.pra.UpdatePraUseCase;

public class PraFacade {

    private final CreatePraUseCase createPraUseCase;
    private final UpdatePraUseCase updatePraUseCase;
    private final DeactivatePraUseCase deactivatePraUseCase;
    private final ActivatePraUseCase activatePraUseCase;

    public PraFacade(CreatePraUseCase createPraUseCase, UpdatePraUseCase updatePraUseCase,
            DeactivatePraUseCase deactivatePraUseCase, ActivatePraUseCase activatePraUseCase) {
        this.createPraUseCase = createPraUseCase;
        this.updatePraUseCase = updatePraUseCase;
        this.deactivatePraUseCase = deactivatePraUseCase;
        this.activatePraUseCase = activatePraUseCase;
    }

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

}
