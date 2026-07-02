package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;

import org.springframework.stereotype.Component;

/**
 * Assemble la représentation de sortie {@link EntrepotDetail} — résout au
 * passage le nom de la région rattachée (dénormalisation en lecture,
 * évitant à chaque consommateur de l'API de refaire l'appel).
 */
@Component
public class EntrepotDetailAssembler {

    private final RegionRepositoryPort regionRepositoryPort;

    public EntrepotDetailAssembler(RegionRepositoryPort regionRepositoryPort) {
        this.regionRepositoryPort = regionRepositoryPort;
    }

    public EntrepotDetail assembler(Entrepot entrepot) {
        String regionNom = entrepot.getRegionId() != null
                ? regionRepositoryPort.findById(entrepot.getRegionId()).map(Region::getNom).orElse(null)
                : null;

        return new EntrepotDetail(
                entrepot.getId().getValue(),
                entrepot.getCode(),
                entrepot.getNom(),
                entrepot.getType(),
                entrepot.getRegionId() != null ? entrepot.getRegionId().getValue() : null,
                regionNom,
                entrepot.getAdresse(),
                entrepot.getTelephone(),
                entrepot.getResponsableUserId(),
                entrepot.isActif(),
                entrepot.getCreatedAt(),
                entrepot.getUpdatedAt());
    }
}
