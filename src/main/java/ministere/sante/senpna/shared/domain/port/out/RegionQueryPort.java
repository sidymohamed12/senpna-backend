package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interroge la source de vérité (base de données) des régions.
 *
 * <p>
 * Défini dans {@code shared} pour rester consultable par n'importe quelle
 * feature sans dépendre du module {@code organisation}, mais implémenté
 * par ce dernier (seul propriétaire de l'agrégat {@code Region}) —
 * symétrique à {@link RoleQueryPort}, implémenté par {@code auth}.
 * </p>
 */
public interface RegionQueryPort {

    List<RegionProjection> findAll();

    Optional<RegionProjection> findByCode(String code);

    Optional<RegionProjection> findById(UUID id);

    boolean existsById(UUID regionId);
}
