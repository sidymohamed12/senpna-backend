package ministere.sante.senpna.stock.infrastructure.persistence.specification;

import ministere.sante.senpna.stock.domain.valueobject.StatutLot;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity_;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity_;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class LotSpecifications {

    private LotSpecifications() {
    }

    public static Specification<LotJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(LotJpaEntity_.numeroLot)), motif);
    }

    public static Specification<LotJpaEntity> medicamentId(UUID medicamentId) {
        if (medicamentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(LotJpaEntity_.medicamentId), medicamentId);
    }

    public static Specification<LotJpaEntity> fournisseurId(UUID fournisseurId) {
        if (fournisseurId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(LotJpaEntity_.fournisseurId), fournisseurId);
    }

    public static Specification<LotJpaEntity> statut(StatutLot statut) {
        if (statut == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(LotJpaEntity_.statut), statut.name());
    }

    public static Specification<LotJpaEntity> expirantAvant(LocalDate dateLimite) {
        if (dateLimite == null) {
            return null;
        }
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(LotJpaEntity_.statut), StatutLot.ACTIF.name()),
                cb.lessThanOrEqualTo(root.get(LotJpaEntity_.dateExpiration), dateLimite));
    }

    /**
     * Scoping organisationnel (cf. {@code EntrepotScopeGuard}) : ne remonte
     * que les lots ayant au moins une ligne de {@link StockJpaEntity} dans
     * l'entrepôt donné — un lot n'a pas d'entrepôt propre, sa « présence »
     * dans un entrepôt se déduit de l'existence d'une ligne de stock.
     */
    public static Specification<LotJpaEntity> possedeStockDansEntrepot(UUID entrepotId) {
        if (entrepotId == null) {
            return null;
        }
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<StockJpaEntity> stockRoot = subquery.from(StockJpaEntity.class);
            subquery.select(stockRoot.get(StockJpaEntity_.id))
                    .where(cb.and(
                            cb.equal(stockRoot.get(StockJpaEntity_.lotId), root.get(LotJpaEntity_.id)),
                            cb.equal(stockRoot.get(StockJpaEntity_.entrepotId), entrepotId)));
            return cb.exists(subquery);
        };
    }

    public static Specification<LotJpaEntity> combiner(String texte, UUID medicamentId, UUID fournisseurId,
            StatutLot statut, UUID entrepotId) {
        Specification<LotJpaEntity> specification = Specification.allOf();

        Specification<LotJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<LotJpaEntity> medicamentSpec = medicamentId(medicamentId);
        if (medicamentSpec != null) {
            specification = specification.and(medicamentSpec);
        }
        Specification<LotJpaEntity> fournisseurSpec = fournisseurId(fournisseurId);
        if (fournisseurSpec != null) {
            specification = specification.and(fournisseurSpec);
        }
        Specification<LotJpaEntity> statutSpec = statut(statut);
        if (statutSpec != null) {
            specification = specification.and(statutSpec);
        }
        Specification<LotJpaEntity> entrepotSpec = possedeStockDansEntrepot(entrepotId);
        if (entrepotSpec != null) {
            specification = specification.and(entrepotSpec);
        }

        return specification;
    }
}
