package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.GetActualiteQuery;
import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.in.GetActualitePubliqueUseCase;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetActualitePubliqueUseCaseImpl implements GetActualitePubliqueUseCase {

    private final ActualiteRepositoryPort actualiteRepositoryPort;
    private final ActualiteDetailAssembler assembler;

    public GetActualitePubliqueUseCaseImpl(ActualiteRepositoryPort actualiteRepositoryPort,
            ActualiteDetailAssembler assembler) {
        this.actualiteRepositoryPort = actualiteRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ActualiteDetail obtenirPublique(GetActualiteQuery query) {
        Actualite actualite = actualiteRepositoryPort.findById(ActualiteId.of(query.actualiteId()))
                .filter(a -> a.getStatut() == StatutActualite.PUBLIE)
                .orElseThrow(ActualiteIntrouvableException::new);
        return assembler.assembler(actualite);
    }
}
