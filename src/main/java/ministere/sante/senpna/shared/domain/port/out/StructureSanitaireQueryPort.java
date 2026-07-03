package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.StructureSanitaireProjection;

import java.util.Optional;
import java.util.UUID;

/**
 * Interroge la source de vérité (base de données) des structures
 * sanitaires, en lecture seule — symétrique à {@link EntrepotQueryPort}.
 * Défini dans {@code shared} pour rester consultable par n'importe quelle
 * feature (notamment {@code catalogue}, pour résoudre la région d'un
 * acteur affecté à une structure sanitaire) sans dépendre du module
 * {@code organisation}, seul propriétaire de l'agrégat
 * {@code StructureSanitaire}.
 */
public interface StructureSanitaireQueryPort {

    Optional<StructureSanitaireProjection> findById(UUID id);
}
