package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.MouvementDetailAssembler;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.exception.PorteeEntrepotInterditeException;
import ministere.sante.senpna.stock.domain.exception.mouvement.MouvementStockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.port.in.GetMouvementUseCase;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetMouvementUseCaseImpl implements GetMouvementUseCase {

    private final MouvementStockRepositoryPort mouvementStockRepositoryPort;
    private final MouvementDetailAssembler mouvementDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public GetMouvementUseCaseImpl(MouvementStockRepositoryPort mouvementStockRepositoryPort,
            MouvementDetailAssembler mouvementDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.mouvementStockRepositoryPort = mouvementStockRepositoryPort;
        this.mouvementDetailAssembler = mouvementDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public MouvementDetail obtenir(GetMouvementQuery query) {
        MouvementStock mouvement = mouvementStockRepositoryPort.findById(MouvementStockId.of(query.mouvementId()))
                .orElseThrow(MouvementStockIntrouvableException::new);

        // Un mouvement est visible pour un acteur PRA s'il implique son
        // entrepôt, que ce soit comme source ou comme destination (ex : il
        // doit voir les transferts qu'il reçoit, pas seulement ceux qu'il émet).
        if (!entrepotScopeGuard.estActeurNational()) {
            UUID entrepotCourant = entrepotScopeGuard.entrepotIdCourant();
            boolean impliqueSource = mouvement.getEntrepotSourceId() != null
                    && mouvement.getEntrepotSourceId().getValue().equals(entrepotCourant);
            boolean impliqueDestination = mouvement.getEntrepotDestinationId() != null
                    && mouvement.getEntrepotDestinationId().getValue().equals(entrepotCourant);
            if (!impliqueSource && !impliqueDestination) {
                throw new PorteeEntrepotInterditeException();
            }
        }

        return mouvementDetailAssembler.assembler(mouvement);
    }
}
