package ministere.sante.senpna.medicament.application.service;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.model.Famille;

import org.springframework.stereotype.Component;

@Component
public class FamilleDetailAssembler {

    public FamilleDetail assembler(Famille famille) {
        return new FamilleDetail(
                famille.getId().getValue(),
                famille.getCode(),
                famille.getLibelle(),
                famille.getDescription(),
                famille.isActif(),
                famille.getCreatedAt(),
                famille.getUpdatedAt());
    }
}
