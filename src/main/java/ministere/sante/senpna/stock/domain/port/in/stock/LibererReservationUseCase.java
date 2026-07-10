package ministere.sante.senpna.stock.domain.port.in.stock;

import ministere.sante.senpna.stock.domain.command.StockCommands.LibererReservationCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;

public interface LibererReservationUseCase {
    StockDetail liberer(LibererReservationCommand command);
}
