package ministere.sante.senpna.commandeachat.infrastructure.persistence.specification;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.CommandeAchatJpaEntity;

import org.springframework.data.jpa.domain.Specification;

public final class CommandeAchatSpecifications {

    private CommandeAchatSpecifications() {
    }

    public static Specification<CommandeAchatJpaEntity> recherche(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("reference")), motif);
    }

    public static Specification<CommandeAchatJpaEntity> statut(StatutCommandeAchat statut) {
        if (statut == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("statut"), statut);
    }

    public static Specification<CommandeAchatJpaEntity> combiner(String texte, StatutCommandeAchat statut) {
        Specification<CommandeAchatJpaEntity> specification = Specification.allOf();

        Specification<CommandeAchatJpaEntity> rechercheSpec = recherche(texte);
        if (rechercheSpec != null) {
            specification = specification.and(rechercheSpec);
        }
        Specification<CommandeAchatJpaEntity> statutSpec = statut(statut);
        if (statutSpec != null) {
            specification = specification.and(statutSpec);
        }

        return specification;
    }
}
