package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.application.service.LotOwnershipGuard;
import ministere.sante.senpna.stock.domain.command.LotCommands.BloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.exception.lot.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.in.lot.BloquerLotUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bloque un lot (mise en quarantaine) — typiquement déclenché suite à une
 * déclaration de pharmacovigilance. Un lot bloqué ne peut plus être réservé ni
 * expédié.
 */
@Service
public class BloquerLotUseCaseImpl implements BloquerLotUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final LotDetailAssembler lotDetailAssembler;
    private final LotOwnershipGuard lotOwnershipGuard;

    public BloquerLotUseCaseImpl(LotRepositoryPort lotRepositoryPort, LotDetailAssembler lotDetailAssembler,
            LotOwnershipGuard lotOwnershipGuard) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.lotDetailAssembler = lotDetailAssembler;
        this.lotOwnershipGuard = lotOwnershipGuard;
    }

    @Override
    @Transactional
    public LotDetail bloquer(BloquerLotCommand command) {
        Lot lot = lotRepositoryPort.findById(LotId.of(command.lotId()))
                .orElseThrow(LotIntrouvableException::new);

        lotOwnershipGuard.verifierAccesLot(lot.getId());

        lot.bloquer();

        Lot saved = lotRepositoryPort.save(lot);
        return lotDetailAssembler.assembler(saved);
    }
}
