package ministere.sante.senpna.medicament.application.service;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.model.Forme;

import org.springframework.stereotype.Component;

@Component
public class FormeDetailAssembler {

    public FormeDetail assembler(Forme forme) {
        return new FormeDetail(
                forme.getId().getValue(),
                forme.getCode(),
                forme.getLibelle(),
                forme.getDescription(),
                forme.isActif(),
                forme.getCreatedAt(),
                forme.getUpdatedAt());
    }
}
