package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.shared.domain.port.out.RegionCachePort;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import org.springframework.stereotype.Component;

/**
 * Assemble la représentation de sortie {@link EntrepotDetail} — résout au
 * passage le nom de la région rattachée via {@link RegionCachePort}
 * (mémoire), évitant un aller-retour SQL par entrepôt affiché (N+1),
 * suivant la même stratégie que {@code UserRoleSummaryResolver} pour les
 * rôles.
 */
@Component
public class EntrepotDetailAssembler {

    private final RegionCachePort regionCachePort;

    public EntrepotDetailAssembler(RegionCachePort regionCachePort) {
        this.regionCachePort = regionCachePort;
    }

    public EntrepotDetail assembler(Entrepot entrepot) {
        String regionNom = entrepot.getRegionId() != null
                ? regionCachePort.findById(entrepot.getRegionId().getValue()).map(RegionProjection::nom).orElse(null)
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
