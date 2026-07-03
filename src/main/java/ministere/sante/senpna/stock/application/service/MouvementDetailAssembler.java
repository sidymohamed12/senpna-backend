package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.model.MouvementStock;

import org.springframework.stereotype.Component;

@Component
public class MouvementDetailAssembler {

    public MouvementDetail assembler(MouvementStock mouvement) {
        return new MouvementDetail(
                mouvement.getId().getValue(),
                mouvement.getTypeMouvement().name(),
                mouvement.getSens().name(),
                mouvement.getEntrepotSourceId() != null ? mouvement.getEntrepotSourceId().getValue() : null,
                mouvement.getEntrepotDestinationId() != null ? mouvement.getEntrepotDestinationId().getValue()
                        : null,
                mouvement.getCommandeId(),
                mouvement.getLotId().getValue(),
                mouvement.getMedicamentId().getValue(),
                mouvement.getQuantite(),
                mouvement.getDateMouvement(),
                mouvement.getReferenceDocument(),
                mouvement.getMotif(),
                mouvement.getUtilisateurId(),
                mouvement.getCreatedAt());
    }
}
