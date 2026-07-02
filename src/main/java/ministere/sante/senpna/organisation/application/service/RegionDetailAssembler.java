package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;
import ministere.sante.senpna.organisation.domain.model.Region;

import org.springframework.stereotype.Component;

@Component
public class RegionDetailAssembler {

    public RegionDetail assembler(Region region) {
        return new RegionDetail(
                region.getId().getValue(),
                region.getCode(),
                region.getNom(),
                region.isActif(),
                region.getCreatedAt(),
                region.getUpdatedAt());
    }
}
