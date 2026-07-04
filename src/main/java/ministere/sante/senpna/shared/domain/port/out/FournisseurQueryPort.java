package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;

import java.util.List;

/**
 * Interroge la source de vérité (base de données) des fournisseurs, en
 * lecture seule — symétrique à {@link RoleQueryPort} / {@link RegionQueryPort}.
 * Distinct de {@link FournisseurCachePort}, qui résout depuis un cache en
 * mémoire alimenté par ce port.
 */
public interface FournisseurQueryPort {

    List<FournisseurProjection> findAll();
}
