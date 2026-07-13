package ministere.sante.senpna.stock.application.facade;

import org.springframework.stereotype.Component;

import ministere.sante.senpna.stock.domain.command.LotCommands.AlertePeremptionQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.BloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.CreerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.DebloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.GetLotQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.ListLotsQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.domain.command.LotCommands.ModifierPrixLotCommand;
import ministere.sante.senpna.stock.domain.port.in.lot.BloquerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.CreerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.DebloquerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.GetLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.ListLotsUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.ListerAlertesPeremptionUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.ModifierPrixLotUseCase;

@Component
public class LotFacade {

    // ── Lot ──────────────────────────────────────────────────────────────
    private final CreerLotUseCase creerLotUseCase;
    private final BloquerLotUseCase bloquerLotUseCase;
    private final DebloquerLotUseCase debloquerLotUseCase;
    private final GetLotUseCase getLotUseCase;
    private final ListLotsUseCase listLotsUseCase;
    private final ListerAlertesPeremptionUseCase listerAlertesPeremptionUseCase;
    private final ModifierPrixLotUseCase modifierPrixLotUseCase;

    public LotFacade(CreerLotUseCase creerLotUseCase, BloquerLotUseCase bloquerLotUseCase,
            DebloquerLotUseCase debloquerLotUseCase, GetLotUseCase getLotUseCase, ListLotsUseCase listLotsUseCase,
            ListerAlertesPeremptionUseCase listerAlertesPeremptionUseCase,
            ModifierPrixLotUseCase modifierPrixLotUseCase) {
        this.creerLotUseCase = creerLotUseCase;
        this.bloquerLotUseCase = bloquerLotUseCase;
        this.debloquerLotUseCase = debloquerLotUseCase;
        this.getLotUseCase = getLotUseCase;
        this.listLotsUseCase = listLotsUseCase;
        this.listerAlertesPeremptionUseCase = listerAlertesPeremptionUseCase;
        this.modifierPrixLotUseCase = modifierPrixLotUseCase;
    }

    public LotDetail creerLot(CreerLotCommand command) {
        return creerLotUseCase.creer(command);
    }

    public LotDetail bloquerLot(BloquerLotCommand command) {
        return bloquerLotUseCase.bloquer(command);
    }

    public LotDetail debloquerLot(DebloquerLotCommand command) {
        return debloquerLotUseCase.debloquer(command);
    }

    public LotDetail obtenirLot(GetLotQuery query) {
        return getLotUseCase.obtenir(query);
    }

    public LotPage listerLots(ListLotsQuery query) {
        return listLotsUseCase.lister(query);
    }

    public LotPage listerAlertesPeremption(AlertePeremptionQuery query) {
        return listerAlertesPeremptionUseCase.lister(query);
    }

    public LotDetail modifierPrixLot(ModifierPrixLotCommand command) {
        return modifierPrixLotUseCase.modifierPrix(command);
    }

}
