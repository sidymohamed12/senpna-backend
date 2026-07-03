package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Interroge la source de vérité (base de données) du référentiel national
 * des médicaments, en lecture seule — symétrique à {@link EntrepotQueryPort}
 * / {@link RegionQueryPort}. Défini dans {@code shared} pour rester
 * consultable par n'importe quelle feature (notamment {@code catalogue},
 * pour enrichir ses lignes agrégées) sans dépendre du module
 * {@code medicament}, seul propriétaire de l'agrégat {@code Medicament}.
 */
public interface MedicamentQueryPort {

    /** Chargement en lot — évite d'interroger le référentiel un médicament à la fois. */
    List<MedicamentProjection> findAllById(Collection<UUID> ids);
}
