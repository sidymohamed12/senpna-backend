package ministere.sante.senpna.stock.application.facade;

import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.ListMouvementsQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;
import ministere.sante.senpna.stock.domain.port.in.mouvement.GetMouvementUseCase;
import ministere.sante.senpna.stock.domain.port.in.mouvement.ListMouvementsUseCase;

public class MouvementStockFacade {

    // ── MouvementStock ───────────────────────────────────────────────────
    private final GetMouvementUseCase getMouvementUseCase;
    private final ListMouvementsUseCase listMouvementsUseCase;

    public MouvementStockFacade(GetMouvementUseCase getMouvementUseCase, ListMouvementsUseCase listMouvementsUseCase) {
        this.getMouvementUseCase = getMouvementUseCase;
        this.listMouvementsUseCase = listMouvementsUseCase;
    }

    public MouvementDetail obtenirMouvement(GetMouvementQuery query) {
        return getMouvementUseCase.obtenir(query);
    }

    public MouvementPage listerMouvements(ListMouvementsQuery query) {
        return listMouvementsUseCase.lister(query);
    }
}
