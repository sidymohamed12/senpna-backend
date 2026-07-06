package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.FamilleProjection;

import java.util.List;

/**
 * Interroge la source de vérité (base de données) des familles
 * thérapeutiques, en lecture seule — symétrique à
 * {@link FormeQueryPort} / {@link FournisseurQueryPort} /
 * {@link RegionQueryPort}. Distinct de {@link FamilleCachePort}, qui
 * résout depuis un cache en mémoire alimenté par ce port.
 */
public interface FamilleQueryPort {

    List<FamilleProjection> findAll();
}
