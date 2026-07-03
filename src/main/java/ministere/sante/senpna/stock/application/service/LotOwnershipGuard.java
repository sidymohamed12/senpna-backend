package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.exception.LotHorsPorteeException;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import org.springframework.stereotype.Component;

/**
 * Complète {@link EntrepotScopeGuard} pour les opérations portant sur un
 * {@link ministere.sante.senpna.stock.domain.model.Lot}, qui n'a pas
 * lui-même d'entrepôt (c'est une entrée de catalogue partagée, pas une
 * ligne physique) : la « portée » d'un lot pour un acteur PRA est donc
 * définie par la présence d'une ligne de
 * {@link ministere.sante.senpna.stock.domain.model.Stock}
 * pour ce lot dans son propre entrepôt — autrement dit, le lot a déjà
 * transité par chez lui.
 *
 * <p>
 * Les acteurs PNA ont une portée illimitée sur le catalogue des lots
 * (gouvernance nationale) — ce contrôle ne s'applique qu'aux acteurs
 * régionaux.
 * </p>
 */
@Component
public class LotOwnershipGuard {

    private final StockRepositoryPort stockRepositoryPort;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public LotOwnershipGuard(StockRepositoryPort stockRepositoryPort, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    /**
     * @throws LotHorsPorteeException si l'acteur est un rôle régional (PRA)
     *                                et qu'aucune ligne de stock pour ce lot
     *                                n'existe dans son entrepôt.
     */
    public void verifierAccesLot(LotId lotId) {
        if (entrepotScopeGuard.estActeurNational()) {
            return;
        }

        EntrepotId entrepotCourant = EntrepotId.of(entrepotScopeGuard.entrepotIdCourant());
        boolean possedeStock = stockRepositoryPort.findByEntrepotIdAndLotId(entrepotCourant, lotId).isPresent();
        if (!possedeStock) {
            throw new LotHorsPorteeException();
        }
    }
}
