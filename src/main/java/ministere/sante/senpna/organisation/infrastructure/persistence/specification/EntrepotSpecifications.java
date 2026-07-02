package ministere.sante.senpna.organisation.infrastructure.persistence.specification;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class EntrepotSpecifications {

    private EntrepotSpecifications() {
    }

    public static Specification<EntrepotJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nom")), motif),
                cb.like(cb.lower(root.get("code")), motif));
    }

    public static Specification<EntrepotJpaEntity> type(TypeEntrepot type) {
        if (type == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<EntrepotJpaEntity> regionId(UUID regionId) {
        if (regionId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("regionId"), regionId);
    }

    public static Specification<EntrepotJpaEntity> actif(Boolean actif) {
        if (actif == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actif"), actif);
    }

    public static Specification<EntrepotJpaEntity> combiner(String texte, TypeEntrepot type, UUID regionId,
            Boolean actif) {
        Specification<EntrepotJpaEntity> specification = Specification.allOf();

        Specification<EntrepotJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<EntrepotJpaEntity> typeSpec = type(type);
        if (typeSpec != null) {
            specification = specification.and(typeSpec);
        }
        Specification<EntrepotJpaEntity> regionSpec = regionId(regionId);
        if (regionSpec != null) {
            specification = specification.and(regionSpec);
        }
        Specification<EntrepotJpaEntity> actifSpec = actif(actif);
        if (actifSpec != null) {
            specification = specification.and(actifSpec);
        }

        return specification;
    }
}
