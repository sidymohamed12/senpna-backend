package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.ConditionnementProjection;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Interroge la source de vérité (base de données) des conditionnements
 * commercialisables (actifs, prix défini), en lecture seule — symétrique
 * à {@link MedicamentQueryPort}. Défini dans {@code shared} pour rester
 * consultable par n'importe quelle feature (notamment {@code catalogue},
 * pour lister les unités d'achat disponibles pour un médicament) sans
 * dépendre du module {@code medicament}, seul propriétaire de l'agrégat
 * {@code Conditionnement}.
 */
public interface ConditionnementQueryPort {

    /**
     * Chargement en lot, un ou plusieurs médicaments à la fois — ne
     * renvoie que les conditionnements actifs ayant un prix défini,
     * triés par niveau croissant.
     */
    List<ConditionnementProjection> findAllVendablesByMedicamentIdIn(Collection<UUID> medicamentIds);
}
