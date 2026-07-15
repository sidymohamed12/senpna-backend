package ministere.sante.senpna.commandeachat.application.facade;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AnnulerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CreateCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ReceptionnerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.RejeterCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ValiderCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.MarquerFacturePayeeCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.RejeterFactureCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ValiderFactureCommand;
import ministere.sante.senpna.commandeachat.domain.port.in.AnnulerCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.CreateCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GetCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListFacturesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.MarquerFacturePayeeUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ReceptionnerCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.RejeterCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.RejeterFactureUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ValiderCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ValiderFactureUseCase;

import org.springframework.stereotype.Component;

/** Façade côté PNA — gestion des commandes d'achat et des factures reçues. */
@Component
public class CommandeAchatFacade {

    private final CreateCommandeAchatUseCase createCommandeAchatUseCase;
    private final ValiderCommandeAchatUseCase validerCommandeAchatUseCase;
    private final RejeterCommandeAchatUseCase rejeterCommandeAchatUseCase;
    private final AnnulerCommandeAchatUseCase annulerCommandeAchatUseCase;
    private final ReceptionnerCommandeAchatUseCase receptionnerCommandeAchatUseCase;
    private final GetCommandeAchatUseCase getCommandeAchatUseCase;
    private final ListCommandeAchatUseCase listCommandeAchatUseCase;
    private final ValiderFactureUseCase validerFactureUseCase;
    private final RejeterFactureUseCase rejeterFactureUseCase;
    private final MarquerFacturePayeeUseCase marquerFacturePayeeUseCase;
    private final ListFacturesUseCase listFacturesUseCase;

    public CommandeAchatFacade(
            CreateCommandeAchatUseCase createCommandeAchatUseCase,
            ValiderCommandeAchatUseCase validerCommandeAchatUseCase,
            RejeterCommandeAchatUseCase rejeterCommandeAchatUseCase,
            AnnulerCommandeAchatUseCase annulerCommandeAchatUseCase,
            ReceptionnerCommandeAchatUseCase receptionnerCommandeAchatUseCase,
            GetCommandeAchatUseCase getCommandeAchatUseCase,
            ListCommandeAchatUseCase listCommandeAchatUseCase,
            ValiderFactureUseCase validerFactureUseCase,
            RejeterFactureUseCase rejeterFactureUseCase,
            MarquerFacturePayeeUseCase marquerFacturePayeeUseCase,
            ListFacturesUseCase listFacturesUseCase) {
        this.createCommandeAchatUseCase = createCommandeAchatUseCase;
        this.validerCommandeAchatUseCase = validerCommandeAchatUseCase;
        this.rejeterCommandeAchatUseCase = rejeterCommandeAchatUseCase;
        this.annulerCommandeAchatUseCase = annulerCommandeAchatUseCase;
        this.receptionnerCommandeAchatUseCase = receptionnerCommandeAchatUseCase;
        this.getCommandeAchatUseCase = getCommandeAchatUseCase;
        this.listCommandeAchatUseCase = listCommandeAchatUseCase;
        this.validerFactureUseCase = validerFactureUseCase;
        this.rejeterFactureUseCase = rejeterFactureUseCase;
        this.marquerFacturePayeeUseCase = marquerFacturePayeeUseCase;
        this.listFacturesUseCase = listFacturesUseCase;
    }

    public CommandeAchatDetail creer(CreateCommandeAchatCommand command) {
        return createCommandeAchatUseCase.creer(command);
    }

    public CommandeAchatDetail valider(ValiderCommandeAchatCommand command) {
        return validerCommandeAchatUseCase.valider(command);
    }

    public CommandeAchatDetail rejeter(RejeterCommandeAchatCommand command) {
        return rejeterCommandeAchatUseCase.rejeter(command);
    }

    public CommandeAchatDetail annuler(AnnulerCommandeAchatCommand command) {
        return annulerCommandeAchatUseCase.annuler(command);
    }

    public CommandeAchatDetail receptionner(ReceptionnerCommandeAchatCommand command) {
        return receptionnerCommandeAchatUseCase.receptionner(command);
    }

    public CommandeAchatDetail obtenir(GetCommandeAchatQuery query) {
        return getCommandeAchatUseCase.obtenir(query);
    }

    public CommandeAchatPage lister(ListCommandeAchatQuery query) {
        return listCommandeAchatUseCase.lister(query);
    }

    public FactureDetail validerFacture(ValiderFactureCommand command) {
        return validerFactureUseCase.valider(command);
    }

    public FactureDetail rejeterFacture(RejeterFactureCommand command) {
        return rejeterFactureUseCase.rejeter(command);
    }

    public FactureDetail marquerFacturePayee(MarquerFacturePayeeCommand command) {
        return marquerFacturePayeeUseCase.marquerPayee(command);
    }

    public FacturePage listerFactures(ListFacturesQuery query) {
        return listFacturesUseCase.lister(query);
    }
}
