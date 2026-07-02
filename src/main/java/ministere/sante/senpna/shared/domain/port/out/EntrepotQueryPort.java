package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;

import java.util.Optional;
import java.util.UUID;

/**
 * Interroge la source de vérité (base de données) des entrepôts, en
 * lecture seule — symétrique à {@link RegionQueryPort}. Défini dans
 * {@code shared} pour rester consultable par n'importe quelle feature
 * (notamment {@code utilisateurs}, pour valider l'entrepôt fourni à la
 * création d'un compte) sans dépendre du module {@code organisation},
 * seul propriétaire de l'agrégat {@code Entrepot}.
 */
public interface EntrepotQueryPort {

    Optional<EntrepotProjection> findById(UUID id);

    boolean existsById(UUID id);
}
