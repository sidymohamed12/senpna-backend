package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.FormeProjection;

import java.util.List;

/**
 * Interroge la source de vérité (base de données) des formes
 * pharmaceutiques, en lecture seule — symétrique à
 * {@link FournisseurQueryPort} / {@link RegionQueryPort}. Distinct de
 * {@link FormeCachePort}, qui résout depuis un cache en mémoire alimenté
 * par ce port.
 */
public interface FormeQueryPort {

    List<FormeProjection> findAll();
}
