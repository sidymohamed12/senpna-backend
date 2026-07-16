package ministere.sante.senpna.actualite.infrastructure.persistence.cache;

import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;
import ministere.sante.senpna.actualite.infrastructure.persistence.cache.ActualiteCacheEntry.MediaEntry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ActualiteCacheEntry — instantané sérialisable d'une Actualite pour le cache")
class ActualiteCacheEntryTest {

    private static final UUID ACTUALITE_ID = UUID.randomUUID();
    private static final UUID AUTEUR_ID = UUID.randomUUID();

    @Test
    @DisplayName("from() capture tous les champs de l'actualité, y compris les médias")
    void from_capturesTousLesChamps() {
        Instant maintenant = Instant.now();
        ActualiteMedia media = ActualiteMedia.reconstruct(UUID.randomUUID(), TypeMedia.IMAGE, "https://cdn/1.png", 0);
        Actualite actualite = Actualite.reconstruct(ActualiteId.of(ACTUALITE_ID), CategorieActualite.PROJET, "Titre",
                "Description", List.of(media), AUTEUR_ID, "Awa Diop", List.of("sante"), StatutActualite.PUBLIE,
                maintenant, maintenant);

        ActualiteCacheEntry entry = ActualiteCacheEntry.from(actualite);

        assertThat(entry.id()).isEqualTo(ACTUALITE_ID);
        assertThat(entry.categorie()).isEqualTo(CategorieActualite.PROJET);
        assertThat(entry.titre()).isEqualTo("Titre");
        assertThat(entry.description()).isEqualTo("Description");
        assertThat(entry.medias()).hasSize(1);
        assertThat(entry.medias().get(0).id()).isEqualTo(media.getId());
        assertThat(entry.medias().get(0).type()).isEqualTo(TypeMedia.IMAGE);
        assertThat(entry.medias().get(0).url()).isEqualTo("https://cdn/1.png");
        assertThat(entry.medias().get(0).ordre()).isZero();
        assertThat(entry.auteurId()).isEqualTo(AUTEUR_ID);
        assertThat(entry.auteurNom()).isEqualTo("Awa Diop");
        assertThat(entry.tags()).containsExactly("sante");
        assertThat(entry.statut()).isEqualTo(StatutActualite.PUBLIE);
        assertThat(entry.createdAt()).isEqualTo(maintenant);
        assertThat(entry.updatedAt()).isEqualTo(maintenant);
    }

    @Test
    @DisplayName("from() sans média produit une liste de médias vide")
    void from_sansMedia_listeVide() {
        Instant maintenant = Instant.now();
        Actualite actualite = Actualite.reconstruct(ActualiteId.of(ACTUALITE_ID), CategorieActualite.AUTRE, "Titre",
                null, List.of(), AUTEUR_ID, "Awa Diop", List.of(), StatutActualite.BROUILLON, maintenant, maintenant);

        ActualiteCacheEntry entry = ActualiteCacheEntry.from(actualite);

        assertThat(entry.medias()).isEmpty();
    }

    @Test
    @DisplayName("toDomain() reconstruit une Actualite équivalente à l'originale")
    void toDomain_reconstruitActualite() {
        Instant maintenant = Instant.now();
        ActualiteMedia media = ActualiteMedia.reconstruct(UUID.randomUUID(), TypeMedia.VIDEO, "https://cdn/2.mp4", 1);
        Actualite original = Actualite.reconstruct(ActualiteId.of(ACTUALITE_ID), CategorieActualite.EVENEMENT,
                "Titre", "Description", List.of(media), AUTEUR_ID, "Awa Diop", List.of("evenement"),
                StatutActualite.DESACTIVE, maintenant, maintenant);

        ActualiteCacheEntry entry = ActualiteCacheEntry.from(original);
        Actualite reconstruit = entry.toDomain();

        assertThat(reconstruit.getId().getValue()).isEqualTo(ACTUALITE_ID);
        assertThat(reconstruit.getCategorie()).isEqualTo(CategorieActualite.EVENEMENT);
        assertThat(reconstruit.getTitre()).isEqualTo("Titre");
        assertThat(reconstruit.getDescription()).isEqualTo("Description");
        assertThat(reconstruit.getMedias()).hasSize(1);
        assertThat(reconstruit.getMedias().get(0).getUrl()).isEqualTo("https://cdn/2.mp4");
        assertThat(reconstruit.getAuteurId()).isEqualTo(AUTEUR_ID);
        assertThat(reconstruit.getAuteurNom()).isEqualTo("Awa Diop");
        assertThat(reconstruit.getTags()).containsExactly("evenement");
        assertThat(reconstruit.getStatut()).isEqualTo(StatutActualite.DESACTIVE);
        assertThat(reconstruit.getCreatedAt()).isEqualTo(maintenant);
        assertThat(reconstruit.getUpdatedAt()).isEqualTo(maintenant);
    }

    @Test
    @DisplayName("MediaEntry.from()/toDomain() font le pont dans les deux sens")
    void mediaEntry_fromEtToDomain() {
        UUID mediaId = UUID.randomUUID();
        ActualiteMedia media = ActualiteMedia.reconstruct(mediaId, TypeMedia.IMAGE, "https://cdn/3.png", 2);

        MediaEntry entry = MediaEntry.from(media);
        assertThat(entry.id()).isEqualTo(mediaId);
        assertThat(entry.type()).isEqualTo(TypeMedia.IMAGE);
        assertThat(entry.url()).isEqualTo("https://cdn/3.png");
        assertThat(entry.ordre()).isEqualTo(2);

        ActualiteMedia reconstruit = entry.toDomain();
        assertThat(reconstruit.getId()).isEqualTo(mediaId);
        assertThat(reconstruit.getType()).isEqualTo(TypeMedia.IMAGE);
        assertThat(reconstruit.getUrl()).isEqualTo("https://cdn/3.png");
        assertThat(reconstruit.getOrdre()).isEqualTo(2);
    }
}
