package ministere.sante.senpna.carriere.infrastructure.persistence.specification;

import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.OpportuniteCarriereJpaEntity;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class OpportuniteCarriereSpecifications {

    private OpportuniteCarriereSpecifications() {
    }

    public static Specification<OpportuniteCarriereJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titre")), motif),
                cb.like(cb.lower(root.get("nomEntreprise")), motif),
                cb.like(cb.lower(root.get("lieu")), motif));
    }

    public static Specification<OpportuniteCarriereJpaEntity> typeContrat(TypeContrat typeContrat) {
        if (typeContrat == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("typeContrat"), typeContrat);
    }

    public static Specification<OpportuniteCarriereJpaEntity> statut(StatutOpportunite statut) {
        if (statut == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("statut"), statut);
    }

    /**
     * Restreint aux offres visibles publiquement : {@code OUVERT} ou
     * {@code EN_COURS}, et date limite non dépassée — matérialise en base
     * la clôture automatique pour la vitrine publique, sans dépendre du
     * job planifié de clôture (cf. {@code OpportuniteCarriere#estExpiree()}).
     */
    public static Specification<OpportuniteCarriereJpaEntity> visiblePubliquement() {
        return (root, query, cb) -> cb.and(
                root.get("statut").in(List.of(StatutOpportunite.OUVERT, StatutOpportunite.EN_COURS)),
                cb.greaterThanOrEqualTo(root.get("dateLimiteCandidature"), LocalDate.now()));
    }

    public static Specification<OpportuniteCarriereJpaEntity> combiner(String texte, TypeContrat typeContrat,
            StatutOpportunite statut, boolean publicOnly) {
        Specification<OpportuniteCarriereJpaEntity> specification = Specification.allOf();

        Specification<OpportuniteCarriereJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<OpportuniteCarriereJpaEntity> typeContratSpec = typeContrat(typeContrat);
        if (typeContratSpec != null) {
            specification = specification.and(typeContratSpec);
        }

        if (publicOnly) {
            specification = specification.and(visiblePubliquement());
        } else {
            Specification<OpportuniteCarriereJpaEntity> statutSpec = statut(statut);
            if (statutSpec != null) {
                specification = specification.and(statutSpec);
            }
        }

        return specification;
    }
}
