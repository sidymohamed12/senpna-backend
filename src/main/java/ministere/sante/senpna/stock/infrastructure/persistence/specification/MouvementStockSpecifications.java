package ministere.sante.senpna.stock.infrastructure.persistence.specification;

import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity_;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class MouvementStockSpecifications {

    private MouvementStockSpecifications() {
    }

    public static Specification<MouvementStockJpaEntity> lotId(UUID lotId) {
        if (lotId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(MouvementStockJpaEntity_.lotId), lotId);
    }

    public static Specification<MouvementStockJpaEntity> medicamentId(UUID medicamentId) {
        if (medicamentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(MouvementStockJpaEntity_.medicamentId), medicamentId);
    }

    /**
     * Un entrepôt donné, qu'il intervienne comme source ou comme destination du
     * mouvement.
     */
    public static Specification<MouvementStockJpaEntity> entrepotId(UUID entrepotId) {
        if (entrepotId == null) {
            return null;
        }
        return (root, query, cb) -> cb.or(
                cb.equal(root.get(MouvementStockJpaEntity_.entrepotSourceId), entrepotId),
                cb.equal(root.get(MouvementStockJpaEntity_.entrepotDestinationId), entrepotId));
    }

    public static Specification<MouvementStockJpaEntity> typeMouvement(TypeMouvement typeMouvement) {
        if (typeMouvement == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(MouvementStockJpaEntity_.typeMouvement), typeMouvement.name());
    }

    public static Specification<MouvementStockJpaEntity> sens(SensMouvement sens) {
        if (sens == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(MouvementStockJpaEntity_.sens), sens.name());
    }

    public static Specification<MouvementStockJpaEntity> utilisateurId(UUID utilisateurId) {
        if (utilisateurId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(MouvementStockJpaEntity_.utilisateurId), utilisateurId);
    }

    public static Specification<MouvementStockJpaEntity> periode(Instant dateDebut, Instant dateFin) {
        if (dateDebut == null && dateFin == null) {
            return null;
        }
        return (root, query, cb) -> {
            if (dateDebut != null && dateFin != null) {
                return cb.between(root.get(MouvementStockJpaEntity_.dateMouvement), dateDebut, dateFin);
            }
            if (dateDebut != null) {
                return cb.greaterThanOrEqualTo(root.get(MouvementStockJpaEntity_.dateMouvement), dateDebut);
            }
            return cb.lessThanOrEqualTo(root.get(MouvementStockJpaEntity_.dateMouvement), dateFin);
        };
    }

    public static Specification<MouvementStockJpaEntity> combiner(UUID lotId, UUID medicamentId, UUID entrepotId,
            TypeMouvement typeMouvement, SensMouvement sens, UUID utilisateurId, Instant dateDebut, Instant dateFin) {
        Specification<MouvementStockJpaEntity> specification = Specification.allOf();

        Specification<MouvementStockJpaEntity> lotSpec = lotId(lotId);
        if (lotSpec != null) {
            specification = specification.and(lotSpec);
        }
        Specification<MouvementStockJpaEntity> medicamentSpec = medicamentId(medicamentId);
        if (medicamentSpec != null) {
            specification = specification.and(medicamentSpec);
        }
        Specification<MouvementStockJpaEntity> entrepotSpec = entrepotId(entrepotId);
        if (entrepotSpec != null) {
            specification = specification.and(entrepotSpec);
        }
        Specification<MouvementStockJpaEntity> typeSpec = typeMouvement(typeMouvement);
        if (typeSpec != null) {
            specification = specification.and(typeSpec);
        }
        Specification<MouvementStockJpaEntity> sensSpec = sens(sens);
        if (sensSpec != null) {
            specification = specification.and(sensSpec);
        }
        Specification<MouvementStockJpaEntity> utilisateurSpec = utilisateurId(utilisateurId);
        if (utilisateurSpec != null) {
            specification = specification.and(utilisateurSpec);
        }
        Specification<MouvementStockJpaEntity> periodeSpec = periode(dateDebut, dateFin);
        if (periodeSpec != null) {
            specification = specification.and(periodeSpec);
        }

        return specification;
    }
}
