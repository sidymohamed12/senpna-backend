package ministere.sante.senpna.actualite.infrastructure.persistence.specification;

import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteJpaEntity;

import org.springframework.data.jpa.domain.Specification;

public final class ActualiteSpecifications {

    private ActualiteSpecifications() {
    }

    public static Specification<ActualiteJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            return cb.or(
                    cb.like(cb.lower(root.get("titre")), motif),
                    cb.like(cb.lower(root.get("description")), motif));
        };
    }

    public static Specification<ActualiteJpaEntity> categorie(CategorieActualite categorie) {
        if (categorie == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("categorie"), categorie);
    }

    public static Specification<ActualiteJpaEntity> statut(StatutActualite statut) {
        if (statut == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("statut"), statut);
    }

    public static Specification<ActualiteJpaEntity> combiner(String texte, CategorieActualite categorie,
            StatutActualite statut) {
        Specification<ActualiteJpaEntity> specification = Specification.allOf();

        Specification<ActualiteJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<ActualiteJpaEntity> categorieSpec = categorie(categorie);
        if (categorieSpec != null) {
            specification = specification.and(categorieSpec);
        }
        Specification<ActualiteJpaEntity> statutSpec = statut(statut);
        if (statutSpec != null) {
            specification = specification.and(statutSpec);
        }

        return specification;
    }
}
