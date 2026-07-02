package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.EntrepotDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.DeactivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.in.DeactivatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivatePraUseCaseImpl implements DeactivatePraUseCase {

    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final EntrepotDetailAssembler entrepotDetailAssembler;

    public DeactivatePraUseCaseImpl(EntrepotRepositoryPort entrepotRepositoryPort,
            EntrepotDetailAssembler entrepotDetailAssembler) {
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.entrepotDetailAssembler = entrepotDetailAssembler;
    }

    @Override
    @Transactional
    public EntrepotDetail desactiver(DeactivatePraCommand command) {
        Entrepot pra = entrepotRepositoryPort.findById(EntrepotId.of(command.entrepotId()))
                .orElseThrow(EntrepotIntrouvableException::new);

        if (!pra.estPra()) {
            throw new TypeEntrepotInvalideException();
        }

        pra.desactiver();

        Entrepot saved = entrepotRepositoryPort.save(pra);
        return entrepotDetailAssembler.assembler(saved);
    }
}
