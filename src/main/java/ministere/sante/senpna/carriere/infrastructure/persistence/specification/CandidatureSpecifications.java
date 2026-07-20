package ministere.sante.senpna.carriere.infrastructure.persistence.specification;

import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.CandidatureJpaEntity;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class CandidatureSpecifications {

    private CandidatureSpecifications() {
    }

    public static Specification<CandidatureJpaEntity> opportunite(UUID opportuniteId) {
        if (opportuniteId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("opportuniteId"), opportuniteId);
    }

    public static Specification<CandidatureJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + LikePatternEscaper.escape(texte.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nomComplet")), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get("email")), motif, LikePatternEscaper.escapeChar()));
    }

    public static Specification<CandidatureJpaEntity> combiner(UUID opportuniteId, String texte) {
        Specification<CandidatureJpaEntity> specification = Specification.allOf();

        Specification<CandidatureJpaEntity> opportuniteSpec = opportunite(opportuniteId);
        if (opportuniteSpec != null) {
            specification = specification.and(opportuniteSpec);
        }
        Specification<CandidatureJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }

        return specification;
    }
}
