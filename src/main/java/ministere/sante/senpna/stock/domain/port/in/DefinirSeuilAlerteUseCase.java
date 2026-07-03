package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.StockCommands.DefinirSeuilAlerteCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;

/** Définit ou modifie le seuil de sécurité d'une ligne de stock. */
public interface DefinirSeuilAlerteUseCase {
    StockDetail definir(DefinirSeuilAlerteCommand command);
}
