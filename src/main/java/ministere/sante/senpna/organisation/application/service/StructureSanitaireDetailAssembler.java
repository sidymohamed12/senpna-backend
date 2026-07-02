package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.RegionCachePort;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import org.springframework.stereotype.Component;

/**
 * Assemble la représentation de sortie {@link StructureSanitaireDetail} —
 * résout le nom de la région via {@link RegionCachePort} (mémoire) et le
 * nom de la PRA de rattachement.
 */
@Component
public class StructureSanitaireDetailAssembler {

        private final RegionCachePort regionCachePort;
        private final EntrepotRepositoryPort entrepotRepositoryPort;

        public StructureSanitaireDetailAssembler(RegionCachePort regionCachePort,
                        EntrepotRepositoryPort entrepotRepositoryPort) {
                this.regionCachePort = regionCachePort;
                this.entrepotRepositoryPort = entrepotRepositoryPort;
        }

        public StructureSanitaireDetail assembler(StructureSanitaire structure) {
                String regionNom = structure.getRegionId() != null
                                ? regionCachePort.findById(structure.getRegionId().getValue())
                                                .map(RegionProjection::nom)
                                                .orElse(null)
                                : null;

                String praNom = structure.getPraId() != null
                                ? entrepotRepositoryPort.findById(structure.getPraId()).map(Entrepot::getNom)
                                                .orElse(null)
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
                                structure.getResponsableNom(),
                                structure.getResponsablePrenom(),
                                structure.getStatutAdhesion(),
                                structure.getMotifRejet(),
                                structure.isActif(),
                                structure.getCreatedAt(),
                                structure.getUpdatedAt());
        }
}
