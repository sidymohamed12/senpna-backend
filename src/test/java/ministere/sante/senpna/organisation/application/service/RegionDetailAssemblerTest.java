package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegionDetailAssembler")
class RegionDetailAssemblerTest {

    RegionDetailAssembler assembler = new RegionDetailAssembler();

    @Test
    @DisplayName("assemble le détail à partir de la région du domaine")
    void assembler_assembleLeDetail() {
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();
        Region region = Region.reconstruct(RegionId.generate(), "DK", "Dakar", true, createdAt, updatedAt);

        RegionDetail detail = assembler.assembler(region);

        assertThat(detail.id()).isEqualTo(region.getId().getValue());
        assertThat(detail.code()).isEqualTo("DK");
        assertThat(detail.nom()).isEqualTo("Dakar");
        assertThat(detail.actif()).isTrue();
        assertThat(detail.createdAt()).isEqualTo(createdAt);
        assertThat(detail.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("région archivée → actif false dans le détail")
    void assembler_regionArchivee_actifFalse() {
        Region region = Region.reconstruct(RegionId.generate(), "SL", "Saint-Louis", false, Instant.now(),
                Instant.now());

        RegionDetail detail = assembler.assembler(region);

        assertThat(detail.actif()).isFalse();
    }
}
