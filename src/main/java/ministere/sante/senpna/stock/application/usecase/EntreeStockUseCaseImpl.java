package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.EntreeStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.exception.lot.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.mouvement.EntreeStockUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Entrée en stock : augmente la quantité disponible de la ligne de stock
 * (entrepôt, lot) — ouverte à zéro si elle n'existe pas encore — et enregistre
 * le {@link MouvementStock} correspondant dans la même transaction.
 */
@Service
public class EntreeStockUseCaseImpl implements EntreeStockUseCase {

    private final StockRepositoryPort stockRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final MouvementStockRepositoryPort mouvementStockRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public EntreeStockUseCaseImpl(StockRepositoryPort stockRepositoryPort, LotRepositoryPort lotRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort, MouvementStockRepositoryPort mouvementStockRepositoryPort,
            StockDetailAssembler stockDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.mouvementStockRepositoryPort = mouvementStockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional
    public StockDetail entrer(EntreeStockCommand command) {
        entrepotScopeGuard.verifierEcritureAutorisee(command.entrepotId());

        Lot lot = lotRepositoryPort.findById(LotId.of(command.lotId())).orElseThrow(LotIntrouvableException::new);

        Entrepot entrepot = entrepotRepositoryPort.findById(EntrepotId.of(command.entrepotId()))
                .orElseThrow(EntrepotIntrouvableException::new);

        TypeMouvement type = parseType(command.typeMouvement());

        Stock stock = stockRepositoryPort
                .findByEntrepotIdAndLotIdForUpdate(entrepot.getId(), lot.getId())
                .orElseGet(() -> Stock.ouvrir(entrepot.getId(), lot.getId(), lot.getMedicamentId(), null));

        stock.entrer(command.quantite());
        Stock saved = stockRepositoryPort.save(stock);

        MouvementStock mouvement = MouvementStock.creer(type, SensMouvement.ENTREE, null, entrepot.getId(),
                command.commandeId(), lot.getId(), lot.getMedicamentId(), command.quantite(),
                command.referenceDocument(), command.motif(), command.utilisateurId());
        mouvementStockRepositoryPort.save(mouvement);

        return stockDetailAssembler.assembler(saved);
    }

    private static TypeMouvement parseType(String typeMouvement) {
        Objects.requireNonNull(typeMouvement, "Le type de mouvement est obligatoire");
        try {
            return TypeMouvement.valueOf(typeMouvement.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Type de mouvement invalide : " + typeMouvement, "TYPE_MOUVEMENT_INVALID");
        }
    }
}
