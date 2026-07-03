package ministere.sante.senpna.medicament.infrastructure.persistence.specification;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class ConditionnementSpecifications {

    private ConditionnementSpecifications() {
    }

    public static Specification<ConditionnementJpaEntity> medicamentId(UUID medicamentId) {
        if (medicamentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("medicamentId"), medicamentId);
    }

    public static Specification<ConditionnementJpaEntity> actif(Boolean actif) {
        if (actif == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actif"), actif);
    }

    public static Specification<ConditionnementJpaEntity> combiner(UUID medicamentId, Boolean actif) {
        Specification<ConditionnementJpaEntity> specification = Specification.allOf();

        Specification<ConditionnementJpaEntity> medicamentSpec = medicamentId(medicamentId);
        if (medicamentSpec != null) {
            specification = specification.and(medicamentSpec);
        }
        Specification<ConditionnementJpaEntity> actifSpec = actif(actif);
        if (actifSpec != null) {
            specification = specification.and(actifSpec);
        }

        return specification;
    }
}
