package ministere.sante.senpna.commandeachat.application.service;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.model.Facture;

import org.springframework.stereotype.Component;

@Component
public class FactureDetailAssembler {

    public FactureDetail assembler(Facture facture) {
        return new FactureDetail(
                facture.getId().getValue(),
                facture.getCommandeAchatId().getValue(),
                facture.getFournisseurId().getValue(),
                facture.getNumeroFacture(),
                facture.getMontant(),
                facture.getDateEmission(),
                facture.getDateEcheance(),
                facture.getPieceJointeMediaId(),
                facture.getStatut(),
                facture.getMotifRejet(),
                facture.getCreatedAt(),
                facture.getUpdatedAt());
    }
}
