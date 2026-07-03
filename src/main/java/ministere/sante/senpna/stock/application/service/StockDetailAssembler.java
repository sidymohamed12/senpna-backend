package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.model.Stock;

import org.springframework.stereotype.Component;

@Component
public class StockDetailAssembler {

    public StockDetail assembler(Stock stock) {
        return new StockDetail(
                stock.getId().getValue(),
                stock.getEntrepotId().getValue(),
                stock.getLotId().getValue(),
                stock.getMedicamentId().getValue(),
                stock.getQuantiteDisponible(),
                stock.getQuantiteReservee(),
                stock.getQuantiteDisponibleALaVente(),
                stock.getQuantiteEnCommande(),
                stock.getSeuilAlerte(),
                stock.estEnRupture(),
                stock.seuilAtteint(),
                stock.getCreatedAt(),
                stock.getUpdatedAt());
    }
}
