package ministere.sante.senpna.stock.domain.port.in.mouvement;

import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.ListMouvementsQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;

public interface ListMouvementsUseCase {
    MouvementPage lister(ListMouvementsQuery query);
}
