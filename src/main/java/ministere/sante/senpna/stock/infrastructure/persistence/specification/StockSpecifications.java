package ministere.sante.senpna.stock.infrastructure.persistence.specification;

import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public final class StockSpecifications {

    private StockSpecifications() {
    }

    public static Specification<StockJpaEntity> entrepotId(UUID entrepotId) {
        if (entrepotId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("entrepotId"), entrepotId);
    }

    public static Specification<StockJpaEntity> lotId(UUID lotId) {
        if (lotId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("lotId"), lotId);
    }

    public static Specification<StockJpaEntity> medicamentId(UUID medicamentId) {
        if (medicamentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("medicamentId"), medicamentId);
    }

    /** Rupture : quantité disponible à la vente (disponible - réservée) ≤ 0. */
    public static Specification<StockJpaEntity> enRupture(Boolean ruptureUniquement) {
        if (!Boolean.TRUE.equals(ruptureUniquement)) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(
                cb.diff(root.get("quantiteDisponible"), root.get("quantiteReservee")), BigDecimal.ZERO);
    }

    /** Seuil d'alerte atteint : quantité disponible à la vente ≤ seuil défini. */
    public static Specification<StockJpaEntity> seuilAtteint(Boolean seuilAtteintUniquement) {
        if (!Boolean.TRUE.equals(seuilAtteintUniquement)) {
            return null;
        }
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("seuilAlerte")),
                cb.lessThanOrEqualTo(
                        cb.diff(root.get("quantiteDisponible"), root.get("quantiteReservee")),
                        root.get("seuilAlerte")));
    }

    public static Specification<StockJpaEntity> combiner(UUID entrepotId, UUID lotId, UUID medicamentId,
            Boolean ruptureUniquement, Boolean seuilAtteintUniquement) {
        Specification<StockJpaEntity> specification = Specification.allOf();

        Specification<StockJpaEntity> entrepotSpec = entrepotId(entrepotId);
        if (entrepotSpec != null) {
            specification = specification.and(entrepotSpec);
        }
        Specification<StockJpaEntity> lotSpec = lotId(lotId);
        if (lotSpec != null) {
            specification = specification.and(lotSpec);
        }
        Specification<StockJpaEntity> medicamentSpec = medicamentId(medicamentId);
        if (medicamentSpec != null) {
            specification = specification.and(medicamentSpec);
        }
        Specification<StockJpaEntity> ruptureSpec = enRupture(ruptureUniquement);
        if (ruptureSpec != null) {
            specification = specification.and(ruptureSpec);
        }
        Specification<StockJpaEntity> seuilSpec = seuilAtteint(seuilAtteintUniquement);
        if (seuilSpec != null) {
            specification = specification.and(seuilSpec);
        }

        return specification;
    }
}
