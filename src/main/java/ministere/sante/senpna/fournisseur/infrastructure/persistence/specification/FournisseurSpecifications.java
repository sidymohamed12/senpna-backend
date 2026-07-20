package ministere.sante.senpna.fournisseur.infrastructure.persistence.specification;

import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;

import org.springframework.data.jpa.domain.Specification;

public final class FournisseurSpecifications {

    private FournisseurSpecifications() {
    }

    public static Specification<FournisseurJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + LikePatternEscaper.escape(texte.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nom")), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get("email")), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get("telephone")), motif, LikePatternEscaper.escapeChar()));
    }

    public static Specification<FournisseurJpaEntity> actif(Boolean actif) {
        if (actif == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actif"), actif);
    }

    public static Specification<FournisseurJpaEntity> combiner(String texte, Boolean actif) {
        Specification<FournisseurJpaEntity> specification = Specification.allOf();

        Specification<FournisseurJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<FournisseurJpaEntity> actifSpec = actif(actif);
        if (actifSpec != null) {
            specification = specification.and(actifSpec);
        }

        return specification;
    }
}
