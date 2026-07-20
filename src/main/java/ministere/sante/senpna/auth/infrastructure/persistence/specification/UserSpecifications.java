package ministere.sante.senpna.auth.infrastructure.persistence.specification;

import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity_;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<UserJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + LikePatternEscaper.escape(texte.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get(UserJpaEntity_.nom)), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get(UserJpaEntity_.prenom)), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get(UserJpaEntity_.email)), motif, LikePatternEscaper.escapeChar()));
    }

    public static Specification<UserJpaEntity> actif(Boolean actif) {
        if (actif == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(UserJpaEntity_.actif), actif);
    }

    public static Specification<UserJpaEntity> possedeRole(UUID roleId) {
        if (roleId == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.isMember(roleId, root.get(UserJpaEntity_.roleIds));
        };
    }

    public static Specification<UserJpaEntity> combiner(String texte, Boolean actif, UUID roleId) {
        Specification<UserJpaEntity> specification = Specification.allOf();

        Specification<UserJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }

        Specification<UserJpaEntity> actifSpec = actif(actif);
        if (actifSpec != null) {
            specification = specification.and(actifSpec);
        }

        Specification<UserJpaEntity> roleSpec = possedeRole(roleId);
        if (roleSpec != null) {
            specification = specification.and(roleSpec);
        }

        return specification;
    }
}
