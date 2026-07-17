package ministere.sante.senpna.actualite.application.service;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.MediaDetail;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ActualiteDetailAssembler")
class ActualiteDetailAssemblerTest {

    ActualiteDetailAssembler assembler = new ActualiteDetailAssembler();

    @Test
    @DisplayName("assemble le détail, y compris les médias, à partir de l'actualité du domaine")
    void assembler_avecMedias_assembleLeDetail() {
        UUID actualiteId = UUID.randomUUID();
        UUID auteurId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();
        ActualiteMedia media = ActualiteMedia.reconstruct(UUID.randomUUID(), TypeMedia.IMAGE,
                "https://cdn.example.com/image.png", 0);
        Actualite actualite = Actualite.builder()
            .id(ActualiteId.of(actualiteId))
            .categorie(CategorieActualite.PROJET)
            .titre("Titre")
            .description("Description")
            .medias(List.of(media))
            .auteurId(auteurId)
            .auteurNom("Awa Diop")
            .tags(List.of("santé", "projet"))
            .statut(StatutActualite.PUBLIE)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

        ActualiteDetail detail = assembler.assembler(actualite);

        assertThat(detail.id()).isEqualTo(actualiteId);
        assertThat(detail.categorie()).isEqualTo("PROJET");
        assertThat(detail.titre()).isEqualTo("Titre");
        assertThat(detail.auteurId()).isEqualTo(auteurId);
        assertThat(detail.auteurNom()).isEqualTo("Awa Diop");
        assertThat(detail.tags()).containsExactly("santé", "projet");
        assertThat(detail.statut()).isEqualTo("PUBLIE");
        assertThat(detail.createdAt()).isEqualTo(createdAt);
        assertThat(detail.updatedAt()).isEqualTo(updatedAt);

        assertThat(detail.medias()).hasSize(1);
        MediaDetail mediaDetail = detail.medias().get(0);
        assertThat(mediaDetail.type()).isEqualTo("IMAGE");
        assertThat(mediaDetail.url()).isEqualTo("https://cdn.example.com/image.png");
        assertThat(mediaDetail.ordre()).isZero();
    }

    @Test
    @DisplayName("actualité sans média → liste de médias vide")
    void assembler_sansMedia_listeVide() {
        Actualite actualite = Actualite.builder()
            .id(ActualiteId.generate())
            .categorie(CategorieActualite.EVENEMENT)
            .titre("Titre")
            .description("Description")
            .medias(List.of())
            .auteurId(UUID.randomUUID())
            .auteurNom("Awa Diop")
            .tags(List.of())
            .statut(StatutActualite.BROUILLON)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        ActualiteDetail detail = assembler.assembler(actualite);

        assertThat(detail.medias()).isEmpty();
    }
}
