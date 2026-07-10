package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.domain.command.StockCommands.AllocationLot;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReservationFefoResult;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockFefoCommand;
import ministere.sante.senpna.stock.domain.exception.stock.StockInsuffisantException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.ReserverStockFefoUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Réservation automatique des quantités appliquant la règle FEFO (First
 * Expired, First Out — cf. doc. métier §8 et §9) : les lots {@code ACTIF}
 * non expirés du médicament sont consommés par date d'expiration
 * croissante jusqu'à satisfaction de la quantité demandée.
 *
 * <p>
 * Opération tout-ou-rien : si la quantité demandée ne peut être
 * entièrement satisfaite par l'ensemble des lots disponibles dans
 * l'entrepôt, {@link StockInsuffisantException} est levée et la
 * transaction est intégralement annulée — aucune réservation partielle
 * n'est conservée.
 * </p>
 */
@Service
public class ReserverStockFefoUseCaseImpl implements ReserverStockFefoUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final StockRepositoryPort stockRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public ReserverStockFefoUseCaseImpl(LotRepositoryPort lotRepositoryPort, StockRepositoryPort stockRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort, EntrepotScopeGuard entrepotScopeGuard) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.stockRepositoryPort = stockRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional
    public ReservationFefoResult reserver(ReserverStockFefoCommand command) {
        if (command.quantiteDemandee() == null || command.quantiteDemandee().signum() <= 0) {
            throw new IllegalArgumentException("La quantité demandée doit être strictement positive");
        }

        entrepotScopeGuard.verifierEcritureAutorisee(command.entrepotId());

        EntrepotId entrepotId = EntrepotId.of(command.entrepotId());
        entrepotRepositoryPort.findById(entrepotId).orElseThrow(EntrepotIntrouvableException::new);

        MedicamentId medicamentId = MedicamentId.of(command.medicamentId());
        List<Lot> lotsFefo = lotRepositoryPort.findActifsNonExpiresParMedicamentTriesFefo(medicamentId);

        BigDecimal restant = command.quantiteDemandee();
        BigDecimal totalAlloue = BigDecimal.ZERO;
        List<AllocationLot> allocations = new ArrayList<>();

        for (Lot lot : lotsFefo) {
            if (restant.signum() <= 0) {
                break;
            }

            Stock stock = stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(entrepotId, lot.getId())
                    .orElse(null);
            if (stock == null) {
                continue;
            }

            BigDecimal disponible = stock.getQuantiteDisponibleALaVente();
            if (disponible.signum() <= 0) {
                continue;
            }

            BigDecimal aAllouer = disponible.min(restant);
            stock.reserver(aAllouer);
            stockRepositoryPort.save(stock);

            allocations.add(new AllocationLot(lot.getId().getValue(), lot.getNumeroLot(), aAllouer));
            totalAlloue = totalAlloue.add(aAllouer);
            restant = restant.subtract(aAllouer);
        }

        if (restant.signum() > 0) {
            // Rollback intégral : aucune réservation partielle n'est conservée.
            throw new StockInsuffisantException(totalAlloue, command.quantiteDemandee());
        }

        return new ReservationFefoResult(command.entrepotId(), command.medicamentId(), command.quantiteDemandee(),
                totalAlloue, allocations);
    }
}
