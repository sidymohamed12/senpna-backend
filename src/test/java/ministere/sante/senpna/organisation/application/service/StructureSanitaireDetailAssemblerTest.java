package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
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
@DisplayName("StructureSanitaireDetailAssembler")
class StructureSanitaireDetailAssemblerTest {

    @Mock
    RegionCachePort regionCachePort;

    @Mock
    EntrepotRepositoryPort entrepotRepositoryPort;

    StructureSanitaireDetailAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new StructureSanitaireDetailAssembler(regionCachePort, entrepotRepositoryPort);
    }

    private StructureSanitaire structureAvecRegionEtPra(UUID regionId, UUID praId) {
        return StructureSanitaire.builder()
            .id(StructureSanitaireId.generate())
            .code("HOP-DKR")
            .nom("Hôpital de Dakar")
            .type(TypeStructureSanitaire.HOPITAL)
            .regionId(regionId != null ? RegionId.of(regionId) : null)
            .praId(praId != null ? EntrepotId.of(praId) : null)
            .district("District")
            .adresse("Adresse")
            .telephone("+221771234567")
            .email("hopital@example.com")
            .responsableNom("Diallo")
            .responsablePrenom("Awa")
            .statutAdhesion(StatutAdhesion.VALIDEE)
            .motifRejet(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    @DisplayName("résout le nom de la région et de la PRA quand les deux sont rattachées")
    void assembler_avecRegionEtPra_resoutLesNoms() {
        UUID regionId = UUID.randomUUID();
        UUID praId = UUID.randomUUID();
        StructureSanitaire structure = structureAvecRegionEtPra(regionId, praId);

        when(regionCachePort.findById(regionId))
                .thenReturn(Optional.of(new RegionProjection(regionId, "DK", "Dakar", true)));
        Entrepot pra = Entrepot.builder()
            .id(EntrepotId.of(praId))
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
        when(entrepotRepositoryPort.findById(EntrepotId.of(praId))).thenReturn(Optional.of(pra));

        StructureSanitaireDetail detail = assembler.assembler(structure);

        assertThat(detail.regionNom()).isEqualTo("Dakar");
        assertThat(detail.praNom()).isEqualTo("PRA Dakar");
        assertThat(detail.code()).isEqualTo("HOP-DKR");
        assertThat(detail.statutAdhesion()).isEqualTo(StatutAdhesion.VALIDEE);
    }

    @Test
    @DisplayName("région ou PRA introuvable dans le cache/repository → noms null")
    void assembler_regionEtPraIntrouvables_nomsNull() {
        UUID regionId = UUID.randomUUID();
        UUID praId = UUID.randomUUID();
        StructureSanitaire structure = structureAvecRegionEtPra(regionId, praId);

        when(regionCachePort.findById(regionId)).thenReturn(Optional.empty());
        when(entrepotRepositoryPort.findById(EntrepotId.of(praId))).thenReturn(Optional.empty());

        StructureSanitaireDetail detail = assembler.assembler(structure);

        assertThat(detail.regionNom()).isNull();
        assertThat(detail.praNom()).isNull();
    }

    @Test
    @DisplayName("structure sans région ni PRA → aucun appel au cache ni au repository")
    void assembler_sansRegionNiPra_aucunAppel() {
        StructureSanitaire structure = structureAvecRegionEtPra(null, null);

        StructureSanitaireDetail detail = assembler.assembler(structure);

        assertThat(detail.regionId()).isNull();
        assertThat(detail.regionNom()).isNull();
        assertThat(detail.praId()).isNull();
        assertThat(detail.praNom()).isNull();
        verifyNoInteractions(regionCachePort);
        verifyNoInteractions(entrepotRepositoryPort);
    }
}
