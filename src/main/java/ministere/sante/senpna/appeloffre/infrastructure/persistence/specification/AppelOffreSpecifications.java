package ministere.sante.senpna.appeloffre.infrastructure.persistence.specification;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.AppelOffreJpaEntity;

import org.springframework.data.jpa.domain.Specification;

public final class AppelOffreSpecifications {

    private AppelOffreSpecifications() {
    }

    public static Specification<AppelOffreJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("reference")), motif),
                cb.like(cb.lower(root.get("objet")), motif));
    }

    public static Specification<AppelOffreJpaEntity> statut(StatutAppelOffre statut) {
        if (statut == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("statut"), statut);
    }

    public static Specification<AppelOffreJpaEntity> combiner(String texte, StatutAppelOffre statut) {
        Specification<AppelOffreJpaEntity> specification = Specification.allOf();

        Specification<AppelOffreJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<AppelOffreJpaEntity> statutSpec = statut(statut);
        if (statutSpec != null) {
            specification = specification.and(statutSpec);
        }

        return specification;
    }
}
