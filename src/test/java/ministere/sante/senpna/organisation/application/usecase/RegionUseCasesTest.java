package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.RegionDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetRegionQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;
import ministere.sante.senpna.organisation.domain.exception.CodeRegionDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.RegionCachePort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Region Use Cases — create / get / list")
class RegionUseCasesTest {

    private static final UUID REGION_ID = UUID.randomUUID();
    private static final RegionDetail DETAIL_FICTIF = new RegionDetail(
            REGION_ID, "DAKAR", "Dakar", true, Instant.now(), Instant.now());

    // ══════════════════════════════════════════════════════════════════════
    // CreateRegionUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("CreateRegionUseCaseImpl")
    class CreateRegionTest {

        @Mock
        RegionRepositoryPort regionRepositoryPort;
        @Mock
        RegionCachePort regionCachePort;
        @Mock
        RegionDetailAssembler regionDetailAssembler;
        @InjectMocks
        CreateRegionUseCaseImpl sut;

        @Test
        @DisplayName("crée la région, sauvegarde et recharge le cache")
        void creer_succes_sauvegardeEtRechargeCache() {
            when(regionRepositoryPort.existsByCode("DAKAR")).thenReturn(false);
            when(regionRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(regionDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            RegionDetail result = sut.creer(new CreateRegionCommand("dakar", "Dakar"));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
            verify(regionCachePort).reload();
        }

        @Test
        @DisplayName("sauvegarde une région avec le code normalisé en majuscules")
        void creer_succes_codeNormalise() {
            when(regionRepositoryPort.existsByCode(any())).thenReturn(false);
            when(regionRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(regionDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<Region> captor = ArgumentCaptor.forClass(Region.class);

            sut.creer(new CreateRegionCommand("thies", "Thiès"));

            verify(regionRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getCode()).isEqualTo("THIES");
        }

        @Test
        @DisplayName("code déjà utilisé → CodeRegionDejaUtiliseException, aucune sauvegarde")
        void creer_codeDejaUtilise_leveException() {
            when(regionRepositoryPort.existsByCode("DAKAR")).thenReturn(true);

            assertThatThrownBy(() -> sut.creer(new CreateRegionCommand("dakar", "Dakar")))
                    .isInstanceOf(CodeRegionDejaUtiliseException.class);

            verify(regionRepositoryPort, never()).save(any());
            verifyNoInteractions(regionCachePort, regionDetailAssembler);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GetRegionUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("GetRegionUseCaseImpl")
    class GetRegionTest {

        @Mock
        RegionRepositoryPort regionRepositoryPort;
        @Mock
        RegionDetailAssembler regionDetailAssembler;
        @InjectMocks
        GetRegionUseCaseImpl sut;

        @Test
        @DisplayName("région trouvée → retourne le détail assemblé")
        void obtenir_succes_retourneDetail() {
            Region region = Region.creer("DAKAR", "Dakar");
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(region));
            when(regionDetailAssembler.assembler(region)).thenReturn(DETAIL_FICTIF);

            RegionDetail result = sut.obtenir(new GetRegionQuery(REGION_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
        }

        @Test
        @DisplayName("région introuvable → RegionIntrouvableException")
        void obtenir_introuvable_leveException() {
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.obtenir(new GetRegionQuery(REGION_ID)))
                    .isInstanceOf(RegionIntrouvableException.class);

            verifyNoInteractions(regionDetailAssembler);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ListRegionsUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ListRegionsUseCaseImpl")
    class ListRegionsTest {

        @Mock
        RegionRepositoryPort regionRepositoryPort;
        @Mock
        RegionDetailAssembler regionDetailAssembler;
        @InjectMocks
        ListRegionsUseCaseImpl sut;

        @Test
        @DisplayName("retourne toutes les régions assemblées")
        void lister_succes_retourneToutesLesRegions() {
            Region dakar = Region.creer("DAKAR", "Dakar");
            Region thies = Region.creer("THIES", "Thiès");
            when(regionRepositoryPort.findAll()).thenReturn(List.of(dakar, thies));
            when(regionDetailAssembler.assembler(dakar)).thenReturn(DETAIL_FICTIF);
            when(regionDetailAssembler.assembler(thies)).thenReturn(DETAIL_FICTIF);

            List<RegionDetail> result = sut.lister();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("aucune région → liste vide")
        void lister_aucuneRegion_listeVide() {
            when(regionRepositoryPort.findAll()).thenReturn(List.of());

            List<RegionDetail> result = sut.lister();

            assertThat(result).isEmpty();
            verifyNoInteractions(regionDetailAssembler);
        }
    }
}
