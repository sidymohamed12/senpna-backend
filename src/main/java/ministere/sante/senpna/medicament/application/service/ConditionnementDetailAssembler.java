package ministere.sante.senpna.medicament.application.service;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;

import org.springframework.stereotype.Component;

@Component
public class ConditionnementDetailAssembler {

    public ConditionnementDetail assembler(Conditionnement conditionnement) {
        return new ConditionnementDetail(
                conditionnement.getId().getValue(),
                conditionnement.getMedicamentId().getValue(),
                conditionnement.getNom(),
                conditionnement.getNiveau(),
                conditionnement.getQuantiteUniteBase(),
                conditionnement.isEstUniteBase(),
                conditionnement.isActif(),
                conditionnement.getCreatedAt(),
                conditionnement.getUpdatedAt());
    }
}
