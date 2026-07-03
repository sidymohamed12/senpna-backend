package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.MouvementDetailAssembler;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.exception.MouvementStockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.port.in.GetMouvementUseCase;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMouvementUseCaseImpl implements GetMouvementUseCase {

    private final MouvementStockRepositoryPort mouvementStockRepositoryPort;
    private final MouvementDetailAssembler mouvementDetailAssembler;

    public GetMouvementUseCaseImpl(MouvementStockRepositoryPort mouvementStockRepositoryPort,
            MouvementDetailAssembler mouvementDetailAssembler) {
        this.mouvementStockRepositoryPort = mouvementStockRepositoryPort;
        this.mouvementDetailAssembler = mouvementDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public MouvementDetail obtenir(GetMouvementQuery query) {
        MouvementStock mouvement = mouvementStockRepositoryPort.findById(MouvementStockId.of(query.mouvementId()))
                .orElseThrow(MouvementStockIntrouvableException::new);
        return mouvementDetailAssembler.assembler(mouvement);
    }
}
