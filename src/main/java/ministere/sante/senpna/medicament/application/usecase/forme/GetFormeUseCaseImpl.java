package ministere.sante.senpna.medicament.application.usecase.forme;

import ministere.sante.senpna.medicament.application.service.FormeDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.GetFormeQuery;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.port.in.forme.GetFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetFormeUseCaseImpl implements GetFormeUseCase {

    private final FormeRepositoryPort formeRepositoryPort;
    private final FormeDetailAssembler formeDetailAssembler;

    public GetFormeUseCaseImpl(FormeRepositoryPort formeRepositoryPort,
            FormeDetailAssembler formeDetailAssembler) {
        this.formeRepositoryPort = formeRepositoryPort;
        this.formeDetailAssembler = formeDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FormeDetail obtenir(GetFormeQuery query) {
        Forme forme = formeRepositoryPort.findById(FormeId.of(query.formeId()))
                .orElseThrow(FormeIntrouvableException::new);
        return formeDetailAssembler.assembler(forme);
    }
}
