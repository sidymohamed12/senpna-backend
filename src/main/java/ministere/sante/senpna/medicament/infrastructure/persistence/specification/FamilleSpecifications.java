package ministere.sante.senpna.medicament.infrastructure.persistence.specification;

import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;

import org.springframework.data.jpa.domain.Specification;

public final class FamilleSpecifications {

    private FamilleSpecifications() {
    }

    public static Specification<FamilleJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + LikePatternEscaper.escape(texte.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get("libelle")), motif, LikePatternEscaper.escapeChar()));
    }

    public static Specification<FamilleJpaEntity> actif(Boolean actif) {
        if (actif == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actif"), actif);
    }

    public static Specification<FamilleJpaEntity> combiner(String texte, Boolean actif) {
        Specification<FamilleJpaEntity> specification = Specification.allOf();

        Specification<FamilleJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<FamilleJpaEntity> actifSpec = actif(actif);
        if (actifSpec != null) {
            specification = specification.and(actifSpec);
        }

        return specification;
    }
}
