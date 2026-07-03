package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.model.Lot;

import org.springframework.stereotype.Component;

@Component
public class LotDetailAssembler {

    public LotDetail assembler(Lot lot) {
        return new LotDetail(
                lot.getId().getValue(),
                lot.getNumeroLot(),
                lot.getMedicamentId().getValue(),
                lot.getFournisseurId().getValue(),
                lot.getDateFabrication(),
                lot.getDateExpiration(),
                lot.getPrixAchat(),
                lot.getPrixVente(),
                lot.getStatut().name(),
                lot.estExpire(),
                lot.joursAvantExpiration(),
                lot.getCreatedAt(),
                lot.getUpdatedAt());
    }
}
