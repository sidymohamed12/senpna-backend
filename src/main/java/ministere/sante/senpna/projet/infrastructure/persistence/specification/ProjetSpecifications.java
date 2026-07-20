package ministere.sante.senpna.projet.infrastructure.persistence.specification;

import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.projet.infrastructure.persistence.entity.ProjetJpaEntity;

import org.springframework.data.jpa.domain.Specification;

public final class ProjetSpecifications {

    private ProjetSpecifications() {
    }

    public static Specification<ProjetJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + LikePatternEscaper.escape(texte.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nom")), motif, LikePatternEscaper.escapeChar()),
                cb.like(cb.lower(root.get("description")), motif, LikePatternEscaper.escapeChar()));
    }

    public static Specification<ProjetJpaEntity> categorie(CategorieProjet categorie) {
        if (categorie == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("categorie"), categorie);
    }

    public static Specification<ProjetJpaEntity> statut(StatutProjet statut) {
        if (statut == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("statut"), statut);
    }

    public static Specification<ProjetJpaEntity> combiner(String texte, CategorieProjet categorie,
            StatutProjet statut) {
        Specification<ProjetJpaEntity> specification = Specification.allOf();

        Specification<ProjetJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<ProjetJpaEntity> categorieSpec = categorie(categorie);
        if (categorieSpec != null) {
            specification = specification.and(categorieSpec);
        }
        Specification<ProjetJpaEntity> statutSpec = statut(statut);
        if (statutSpec != null) {
            specification = specification.and(statutSpec);
        }

        return specification;
    }
}
