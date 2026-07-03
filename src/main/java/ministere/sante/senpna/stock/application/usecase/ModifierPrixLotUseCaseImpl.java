package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.LotCommands.ModifierPrixLotCommand;
import ministere.sante.senpna.stock.domain.exception.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.in.ModifierPrixLotUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModifierPrixLotUseCaseImpl implements ModifierPrixLotUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final LotDetailAssembler lotDetailAssembler;

    public ModifierPrixLotUseCaseImpl(LotRepositoryPort lotRepositoryPort, LotDetailAssembler lotDetailAssembler) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.lotDetailAssembler = lotDetailAssembler;
    }

    @Override
    @Transactional
    public LotDetail modifierPrix(ModifierPrixLotCommand command) {
        Lot lot = lotRepositoryPort.findById(LotId.of(command.lotId())).orElseThrow(LotIntrouvableException::new);

        lot.modifierPrix(command.prixAchat(), command.prixVente());

        Lot saved = lotRepositoryPort.save(lot);
        return lotDetailAssembler.assembler(saved);
    }
}
