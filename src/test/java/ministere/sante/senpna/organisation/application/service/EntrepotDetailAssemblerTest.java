package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.shared.domain.port.out.RegionCachePort;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EntrepotDetailAssembler")
class EntrepotDetailAssemblerTest {

    @Mock
    RegionCachePort regionCachePort;

    EntrepotDetailAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new EntrepotDetailAssembler(regionCachePort);
    }

    @Test
    @DisplayName("résout le nom de la région via le cache quand l'entrepôt en a une")
    void assembler_avecRegion_resoutLeNom() {
        UUID regionId = UUID.randomUUID();
        Entrepot entrepot = Entrepot.builder()
            .id(EntrepotId.generate())
            .code("PNA-CENTRAL")
            .nom("Pharmacie Nationale")
            .type(TypeEntrepot.PNA_CENTRAL)
            .regionId(RegionId.of(regionId))
            .adresse("Adresse")
            .telephone("+221771234567")
            .responsableUserId(UUID.randomUUID())
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(regionCachePort.findById(regionId))
                .thenReturn(Optional.of(new RegionProjection(regionId, "DK", "Dakar", true)));

        EntrepotDetail detail = assembler.assembler(entrepot);

        assertThat(detail.regionId()).isEqualTo(regionId);
        assertThat(detail.regionNom()).isEqualTo("Dakar");
        assertThat(detail.code()).isEqualTo("PNA-CENTRAL");
        assertThat(detail.type()).isEqualTo(TypeEntrepot.PNA_CENTRAL);
    }

    @Test
    @DisplayName("region introuvable dans le cache → regionNom null")
    void assembler_regionIntrouvableDansLeCache_regionNomNull() {
        UUID regionId = UUID.randomUUID();
        Entrepot entrepot = Entrepot.builder()
            .id(EntrepotId.generate())
            .code("PRA-DAKAR")
            .nom("PRA Dakar")
            .type(TypeEntrepot.PRA)
            .regionId(RegionId.of(regionId))
            .adresse(null)
            .telephone(null)
            .responsableUserId(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(regionCachePort.findById(regionId)).thenReturn(Optional.empty());

        EntrepotDetail detail = assembler.assembler(entrepot);

        assertThat(detail.regionNom()).isNull();
    }

    @Test
    @DisplayName("entrepôt sans région → regionId et regionNom null, sans appel au cache")
    void assembler_sansRegion_regionNullSansAppelCache() {
        Entrepot entrepot = Entrepot.builder()
            .id(EntrepotId.generate())
            .code("PNA-CENTRAL")
            .nom("Pharmacie Nationale")
            .type(TypeEntrepot.PNA_CENTRAL)
            .regionId(null)
            .adresse(null)
            .telephone(null)
            .responsableUserId(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        EntrepotDetail detail = assembler.assembler(entrepot);

        assertThat(detail.regionId()).isNull();
        assertThat(detail.regionNom()).isNull();
        verifyNoInteractions(regionCachePort);
    }
}
