package ministere.sante.senpna.medicament.infrastructure.persistence.specification;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class MedicamentSpecifications {

    private MedicamentSpecifications() {
    }

    public static Specification<MedicamentJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), motif),
                cb.like(cb.lower(root.get("nomCommercial")), motif),
                cb.like(cb.lower(root.get("dci")), motif));
    }

    public static Specification<MedicamentJpaEntity> familleId(UUID familleId) {
        if (familleId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("familleId"), familleId);
    }

    public static Specification<MedicamentJpaEntity> formeId(UUID formeId) {
        if (formeId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("formeId"), formeId);
    }

    public static Specification<MedicamentJpaEntity> actif(Boolean actif) {
        if (actif == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actif"), actif);
    }

    public static Specification<MedicamentJpaEntity> combiner(String texte, UUID familleId, UUID formeId,
            Boolean actif) {
        Specification<MedicamentJpaEntity> specification = Specification.allOf();

        Specification<MedicamentJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<MedicamentJpaEntity> familleSpec = familleId(familleId);
        if (familleSpec != null) {
            specification = specification.and(familleSpec);
        }
        Specification<MedicamentJpaEntity> formeSpec = formeId(formeId);
        if (formeSpec != null) {
            specification = specification.and(formeSpec);
        }
        Specification<MedicamentJpaEntity> actifSpec = actif(actif);
        if (actifSpec != null) {
            specification = specification.and(actifSpec);
        }

        return specification;
    }
}
