package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.EntrepotDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetEntrepotQuery;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.in.GetEntrepotUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetEntrepotUseCaseImpl implements GetEntrepotUseCase {

    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final EntrepotDetailAssembler entrepotDetailAssembler;

    public GetEntrepotUseCaseImpl(EntrepotRepositoryPort entrepotRepositoryPort,
            EntrepotDetailAssembler entrepotDetailAssembler) {
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.entrepotDetailAssembler = entrepotDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public EntrepotDetail obtenir(GetEntrepotQuery query) {
        Entrepot entrepot = entrepotRepositoryPort.findById(EntrepotId.of(query.entrepotId()))
                .orElseThrow(EntrepotIntrouvableException::new);
        return entrepotDetailAssembler.assembler(entrepot);
    }
}
