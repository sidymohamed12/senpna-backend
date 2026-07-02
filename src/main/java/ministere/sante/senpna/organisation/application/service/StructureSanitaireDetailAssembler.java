package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;

import org.springframework.stereotype.Component;

/**
 * Assemble la représentation de sortie {@link StructureSanitaireDetail} —
 * résout les noms de la région et de la PRA de rattachement.
 */
@Component
public class StructureSanitaireDetailAssembler {

    private final RegionRepositoryPort regionRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;

    public StructureSanitaireDetailAssembler(RegionRepositoryPort regionRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort) {
        this.regionRepositoryPort = regionRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
    }

    public StructureSanitaireDetail assembler(StructureSanitaire structure) {
        String regionNom = structure.getRegionId() != null
                ? regionRepositoryPort.findById(structure.getRegionId()).map(Region::getNom).orElse(null)
                : null;

        String praNom = structure.getPraId() != null
                ? entrepotRepositoryPort.findById(structure.getPraId()).map(Entrepot::getNom).orElse(null)
                : null;

        return new StructureSanitaireDetail(
                structure.getId().getValue(),
                structure.getCode(),
                structure.getNom(),
                structure.getType(),
                structure.getRegionId() != null ? structure.getRegionId().getValue() : null,
                regionNom,
                structure.getPraId() != null ? structure.getPraId().getValue() : null,
                praNom,
                structure.getDistrict(),
                structure.getAdresse(),
                structure.getTelephone(),
                structure.getEmail(),
                structure.getResponsable(),
                structure.getStatutAdhesion(),
                structure.getMotifRejet(),
                structure.isActif(),
                structure.getCreatedAt(),
                structure.getUpdatedAt());
    }
}
