package ministere.sante.senpna.actualite.infrastructure.persistence.cache;

import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Instantané JSON-sérialisable d'une {@link Actualite} — le cache
 * applicatif (Redis) ne connaît que des chaînes ; plutôt que de faire
 * porter des annotations Jackson par le modèle de domaine (violation de la
 * Clean Architecture), cette classe fait le pont, symétriquement à ce que
 * {@code MedicamentCacheEntry} fait pour le module {@code medicament}.
 *
 * <p>
 * Les médias ({@link ActualiteMedia}, value object sans identité de
 * persistance propre au sens du cache) sont représentés par le
 * sous-instantané {@link MediaEntry} plutôt que sérialisés directement,
 * pour ne pas dépendre de la disponibilité d'un constructeur Jackson sur
 * le modèle de domaine.
 * </p>
 */
public record ActualiteCacheEntry(
        UUID id,
        CategorieActualite categorie,
        String titre,
        String description,
        List<MediaEntry> medias,
        UUID auteurId,
        String auteurNom,
        List<String> tags,
        StatutActualite statut,
        Instant createdAt,
        Instant updatedAt) {

    public record MediaEntry(UUID id, TypeMedia type, String url, int ordre) {

        static MediaEntry from(ActualiteMedia media) {
            return new MediaEntry(media.getId(), media.getType(), media.getUrl(), media.getOrdre());
        }

        ActualiteMedia toDomain() {
            return ActualiteMedia.reconstruct(id, type, url, ordre);
        }
    }

    public static ActualiteCacheEntry from(Actualite actualite) {
        return new ActualiteCacheEntry(
                actualite.getId().getValue(),
                actualite.getCategorie(),
                actualite.getTitre(),
                actualite.getDescription(),
                actualite.getMedias().stream().map(MediaEntry::from).toList(),
                actualite.getAuteurId(),
                actualite.getAuteurNom(),
                actualite.getTags(),
                actualite.getStatut(),
                actualite.getCreatedAt(),
                actualite.getUpdatedAt());
    }

    public Actualite toDomain() {
        return Actualite.builder()
            .id(ActualiteId.of(id))
            .categorie(categorie)
            .titre(titre)
            .description(description)
            .medias(medias.stream().map(MediaEntry::toDomain).toList())
            .auteurId(auteurId)
            .auteurNom(auteurNom)
            .tags(tags)
            .statut(statut)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
