package ministere.sante.senpna.organisation.infrastructure.persistence.specification;

import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class StructureSanitaireSpecifications {

    private StructureSanitaireSpecifications() {
    }

    public static Specification<StructureSanitaireJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + LikePatternEscaper.escape(texte.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nom")), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get("code")), motif, LikePatternEscaper.escapeChar()));
    }

    public static Specification<StructureSanitaireJpaEntity> type(TypeStructureSanitaire type) {
        if (type == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<StructureSanitaireJpaEntity> regionId(UUID regionId) {
        if (regionId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("regionId"), regionId);
    }

    public static Specification<StructureSanitaireJpaEntity> praId(UUID praId) {
        if (praId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("praId"), praId);
    }

    public static Specification<StructureSanitaireJpaEntity> statutAdhesion(StatutAdhesion statutAdhesion) {
        if (statutAdhesion == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("statutAdhesion"), statutAdhesion);
    }

    public static Specification<StructureSanitaireJpaEntity> actif(Boolean actif) {
        if (actif == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actif"), actif);
    }

    public static Specification<StructureSanitaireJpaEntity> combiner(String texte, TypeStructureSanitaire type,
            UUID regionId, UUID praId, StatutAdhesion statutAdhesion, Boolean actif) {
        Specification<StructureSanitaireJpaEntity> specification = Specification.allOf();

        Specification<StructureSanitaireJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<StructureSanitaireJpaEntity> typeSpec = type(type);
        if (typeSpec != null) {
            specification = specification.and(typeSpec);
        }
        Specification<StructureSanitaireJpaEntity> regionSpec = regionId(regionId);
        if (regionSpec != null) {
            specification = specification.and(regionSpec);
        }
        Specification<StructureSanitaireJpaEntity> praSpec = praId(praId);
        if (praSpec != null) {
            specification = specification.and(praSpec);
        }
        Specification<StructureSanitaireJpaEntity> statutSpec = statutAdhesion(statutAdhesion);
        if (statutSpec != null) {
            specification = specification.and(statutSpec);
        }
        Specification<StructureSanitaireJpaEntity> actifSpec = actif(actif);
        if (actifSpec != null) {
            specification = specification.and(actifSpec);
        }

        return specification;
    }
}
