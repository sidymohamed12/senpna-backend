package ministere.sante.senpna.stock.domain.port.in.mouvement;

import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;

public interface GetMouvementUseCase {
    MouvementDetail obtenir(GetMouvementQuery query);
}
