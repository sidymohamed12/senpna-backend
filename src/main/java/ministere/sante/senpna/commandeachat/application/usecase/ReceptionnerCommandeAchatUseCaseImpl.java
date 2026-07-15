package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.InfoReceptionLigneInput;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ReceptionnerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat.InfoReceptionLigne;
import ministere.sante.senpna.commandeachat.domain.port.in.ReceptionnerCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.LigneCommandeAchatId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Réception (totale ou partielle) d'un bon de commande par la PNA (cf.
 * doc. métier §11 « Gestion des réceptions »).
 *
 * <p>
 * La génération automatique des entrées de stock consécutives à une
 * réception (cf. doc. métier §11 « Génération automatique des entrées en
 * stock ») est délibérément hors du périmètre de ce use case : elle
 * relève du module {@code stock}, qui expose ses propres ports d'entrée
 * (ex: {@code EntreeStockUseCase}). Ce use case se limite à faire
 * évoluer l'état de la commande ; l'orchestration inter-modules
 * (déclencher l'entrée en stock après réception) est un choix
 * d'intégration à trancher séparément (appel direct, événement de
 * domaine...), non traité ici pour ne pas coupler prématurément les deux
 * agrégats.
 * </p>
 */
@Service
public class ReceptionnerCommandeAchatUseCaseImpl implements ReceptionnerCommandeAchatUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public ReceptionnerCommandeAchatUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional
    public CommandeAchatDetail receptionner(ReceptionnerCommandeAchatCommand command) {
        CommandeAchat commande = commandeAchatRepositoryPort.findById(CommandeAchatId.of(command.commandeAchatId()))
                .orElseThrow(CommandeAchatIntrouvableException::new);

        commande.receptionner(command.lignes().stream().map(this::toInfo).toList());

        CommandeAchat saved = commandeAchatRepositoryPort.save(commande);
        return commandeAchatDetailAssembler.assembler(saved);
    }

    private InfoReceptionLigne toInfo(InfoReceptionLigneInput input) {
        return new InfoReceptionLigne(LigneCommandeAchatId.of(input.ligneId()), input.quantiteRecue(),
                input.quantiteRefusee(), input.motifRefus());
    }
}
