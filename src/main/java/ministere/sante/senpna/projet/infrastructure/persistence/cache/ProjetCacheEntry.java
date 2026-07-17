package ministere.sante.senpna.projet.infrastructure.persistence.cache;

import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Instantané JSON-sérialisable d'un {@link Projet} — le cache applicatif
 * (Redis) ne connaît que des chaînes ; plutôt que de faire porter des
 * annotations Jackson par le modèle de domaine (violation de la Clean
 * Architecture), cette classe fait le pont, symétriquement à ce que
 * {@code MedicamentCacheEntry} fait pour le module {@code medicament}.
 */
public record ProjetCacheEntry(
        UUID id,
        CategorieProjet categorie,
        String nom,
        String description,
        List<String> objectifs,
        List<String> impacts,
        String imageUrl,
        StatutProjet statut,
        Instant createdAt,
        Instant updatedAt) {

    public static ProjetCacheEntry from(Projet projet) {
        return new ProjetCacheEntry(
                projet.getId().getValue(),
                projet.getCategorie(),
                projet.getNom(),
                projet.getDescription(),
                projet.getObjectifs(),
                projet.getImpacts(),
                projet.getImageUrl(),
                projet.getStatut(),
                projet.getCreatedAt(),
                projet.getUpdatedAt());
    }

    public Projet toDomain() {
        return Projet.builder()
            .id(ProjetId.of(id))
            .categorie(categorie)
            .nom(nom)
            .description(description)
            .objectifs(objectifs)
            .impacts(impacts)
            .imageUrl(imageUrl)
            .statut(statut)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
