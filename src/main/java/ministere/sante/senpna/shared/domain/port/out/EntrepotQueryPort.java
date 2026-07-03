package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interroge la source de vérité (base de données) des entrepôts, en
 * lecture seule — symétrique à {@link RegionQueryPort}. Défini dans
 * {@code shared} pour rester consultable par n'importe quelle feature
 * (notamment {@code utilisateurs}, pour valider l'entrepôt fourni à la
 * création d'un compte, ou {@code catalogue}, pour localiser la PNA
 * centrale et les PRA actives) sans dépendre du module {@code organisation},
 * seul propriétaire de l'agrégat {@code Entrepot}.
 */
public interface EntrepotQueryPort {

    Optional<EntrepotProjection> findById(UUID id);

    boolean existsById(UUID id);

    /**
     * L'unique entrepôt {@code PNA_CENTRAL} actif — donnée de référence
     * provisionnée par migration.
     */
    Optional<EntrepotProjection> findPnaCentraleActive();

    /**
     * Toutes les PRA actives, tous rattachements régionaux confondus (catalogue
     * inter-PRA).
     */
    List<EntrepotProjection> findPrasActives();

    /** La ou les PRA actives d'une région donnée (catalogue régional). */
    List<EntrepotProjection> findPrasActivesParRegion(UUID regionId);
}
