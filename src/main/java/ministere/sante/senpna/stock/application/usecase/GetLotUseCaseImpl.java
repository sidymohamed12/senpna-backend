package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.application.service.LotOwnershipGuard;
import ministere.sante.senpna.stock.domain.command.LotCommands.GetLotQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.exception.lot.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.in.GetLotUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetLotUseCaseImpl implements GetLotUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final LotDetailAssembler lotDetailAssembler;
    private final LotOwnershipGuard lotOwnershipGuard;

    public GetLotUseCaseImpl(LotRepositoryPort lotRepositoryPort, LotDetailAssembler lotDetailAssembler,
            LotOwnershipGuard lotOwnershipGuard) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.lotDetailAssembler = lotDetailAssembler;
        this.lotOwnershipGuard = lotOwnershipGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public LotDetail obtenir(GetLotQuery query) {
        Lot lot = lotRepositoryPort.findById(LotId.of(query.lotId())).orElseThrow(LotIntrouvableException::new);
        lotOwnershipGuard.verifierAccesLot(lot.getId());
        return lotDetailAssembler.assembler(lot);
    }
}
