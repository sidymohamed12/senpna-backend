package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.EntrepotDetailAssembler;
import ministere.sante.senpna.organisation.application.service.RegionScopeResolver;
import ministere.sante.senpna.organisation.application.usecase.entrepot.GetEntrepotUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.entrepot.ListEntrepotsUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.pharmacieregional.ActivatePraUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.pharmacieregional.CreatePraUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.pharmacieregional.DeactivatePraUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.pharmacieregional.UpdatePraUseCaseImpl;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ActivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.CreatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.DeactivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.Entrepot.GetEntrepotQuery;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ListEntrepotsQuery;
import ministere.sante.senpna.organisation.domain.command.Entrepot.UpdatePraCommand;
import ministere.sante.senpna.organisation.domain.exception.CodeEntrepotDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.RegionInactiveException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Entrepot/PRA Use Cases — create / update / activate / deactivate / get / list")
class EntrepotUseCasesTest {

    private static final UUID ENTREPOT_ID = UUID.randomUUID();
    private static final UUID REGION_ID = UUID.randomUUID();
    private static final UUID ACTEUR_ID = UUID.randomUUID();
    private static final EntrepotDetail DETAIL_FICTIF = new EntrepotDetail(
            ENTREPOT_ID, "PRA-DAKAR", "PRA Dakar", TypeEntrepot.PRA, REGION_ID, "Dakar", null, null, null, true,
            Instant.now(), Instant.now());

    private Region regionActive() {
        return Region.creer("DAKAR", "Dakar");
    }

    private Entrepot praExistante() {
        return Entrepot.reconstruct(EntrepotId.of(ENTREPOT_ID), "PRA-DAKAR", "PRA Dakar", TypeEntrepot.PRA,
                RegionId.of(REGION_ID), null, null, null, true, Instant.now(), Instant.now());
    }

    private Entrepot pnaCentraleExistante() {
        return Entrepot.reconstruct(EntrepotId.of(ENTREPOT_ID), "PNA-CENTRAL", "PNA Centrale",
                TypeEntrepot.PNA_CENTRAL, null, null, null, null, true, Instant.now(), Instant.now());
    }

    // ══════════════════════════════════════════════════════════════════════
    // CreatePraUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("CreatePraUseCaseImpl")
    class CreatePraTest {

        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @Mock
        RegionRepositoryPort regionRepositoryPort;
        @Mock
        EntrepotDetailAssembler entrepotDetailAssembler;
        @InjectMocks
        CreatePraUseCaseImpl sut;

        @Test
        @DisplayName("crée la PRA quand la région existe et est active")
        void creer_succes_creeLaPra() {
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionActive()));
            when(entrepotRepositoryPort.existsByCode(any())).thenReturn(false);
            when(entrepotRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(entrepotDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            EntrepotDetail result = sut.creer(new CreatePraCommand("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
        }

        @Test
        @DisplayName("regionId null → ValidationException")
        void creer_sansRegionId_leveException() {
            var createPraCommand = new CreatePraCommand("PRA-X", "PRA X", null, null, null);
            assertThatThrownBy(() -> sut.creer(createPraCommand))
                    .isInstanceOf(SenPnaException.class);

            verifyNoInteractions(regionRepositoryPort, entrepotRepositoryPort);
        }

        @Test
        @DisplayName("région introuvable → RegionIntrouvableException")
        void creer_regionIntrouvable_leveException() {
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var createPraCommand = new CreatePraCommand("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null);
            assertThatThrownBy(
                    () -> sut.creer(createPraCommand))
                    .isInstanceOf(RegionIntrouvableException.class);

            verify(entrepotRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("région inactive → RegionInactiveException")
        void creer_regionInactive_leveException() {
            Region regionInactive = Region.creer("DAKAR", "Dakar");
            regionInactive.desactiver();
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionInactive));

            var createPraCommand = new CreatePraCommand("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null);
            assertThatThrownBy(
                    () -> sut.creer(createPraCommand))
                    .isInstanceOf(RegionInactiveException.class);
        }

        @Test
        @DisplayName("code déjà utilisé → CodeEntrepotDejaUtiliseException")
        void creer_codeDejaUtilise_leveException() {
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionActive()));
            when(entrepotRepositoryPort.existsByCode(any())).thenReturn(true);

            var createPraCommand = new CreatePraCommand("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null);
            assertThatThrownBy(
                    () -> sut.creer(createPraCommand))
                    .isInstanceOf(CodeEntrepotDejaUtiliseException.class);

            verify(entrepotRepositoryPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UpdatePraUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UpdatePraUseCaseImpl")
    class UpdatePraTest {

        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @Mock
        RegionRepositoryPort regionRepositoryPort;
        @Mock
        RegionScopeResolver regionScopeResolver;
        @Mock
        EntrepotDetailAssembler entrepotDetailAssembler;
        @InjectMocks
        UpdatePraUseCaseImpl sut;

        @Test
        @DisplayName("modifie la PRA après vérification de la portée régionale")
        void modifier_succes_metAJourPra() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(praExistante()));
            when(entrepotRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(entrepotDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            EntrepotDetail result = sut.modifier(
                    new UpdatePraCommand(ENTREPOT_ID, ACTEUR_ID, "PRA Dakar V2", null, null, null));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
            verify(regionScopeResolver).verifierAccesRegion(ACTEUR_ID, RegionId.of(REGION_ID));
        }

        @Test
        @DisplayName("entrepôt introuvable → EntrepotIntrouvableException")
        void modifier_introuvable_leveException() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var updatePraCommand = new UpdatePraCommand(ENTREPOT_ID, ACTEUR_ID, "Nom", null, null, null);
            assertThatThrownBy(() -> sut.modifier(updatePraCommand))
                    .isInstanceOf(EntrepotIntrouvableException.class);
        }

        @Test
        @DisplayName("entrepôt de type PNA_CENTRAL → TypeEntrepotInvalideException")
        void modifier_typePnaCentral_leveException() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(pnaCentraleExistante()));

            var updatePraCommand = new UpdatePraCommand(ENTREPOT_ID, ACTEUR_ID, "Nom", null, null, null);
            assertThatThrownBy(() -> sut.modifier(updatePraCommand))
                    .isInstanceOf(TypeEntrepotInvalideException.class);

            verifyNoInteractions(regionScopeResolver);
        }

        @Test
        @DisplayName("acteur d'une autre région → l'exception du RegionScopeResolver remonte")
        void modifier_accesRegionRefuse_exceptionRemonte() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(praExistante()));
            doThrow(new ministere.sante.senpna.organisation.domain.exception.AccesRegionRefuseException())
                    .when(regionScopeResolver).verifierAccesRegion(any(), any());

            var updatePraCommand = new UpdatePraCommand(ENTREPOT_ID, ACTEUR_ID, "Nom", null, null, null);
            assertThatThrownBy(() -> sut.modifier(updatePraCommand))
                    .isInstanceOf(
                            ministere.sante.senpna.organisation.domain.exception.AccesRegionRefuseException.class);

            verify(entrepotRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("nouvelle région inactive → RegionInactiveException")
        void modifier_nouvelleRegionInactive_leveException() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(praExistante()));
            Region regionInactive = Region.creer("THIES", "Thiès");
            regionInactive.desactiver();
            UUID nouvelleRegionId = UUID.randomUUID();
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionInactive));

            var updatePraCommand = new UpdatePraCommand(ENTREPOT_ID, ACTEUR_ID, "Nom", null, null, nouvelleRegionId);
            assertThatThrownBy(() -> sut.modifier(updatePraCommand))
                    .isInstanceOf(RegionInactiveException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DeactivatePraUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("DeactivatePraUseCaseImpl")
    class DeactivatePraTest {

        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @Mock
        RegionScopeResolver regionScopeResolver;
        @Mock
        EntrepotDetailAssembler entrepotDetailAssembler;
        @InjectMocks
        DeactivatePraUseCaseImpl sut;

        @Test
        @DisplayName("désactive la PRA après vérification de la portée régionale")
        void desactiver_succes_desactivePra() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(praExistante()));
            when(entrepotRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(entrepotDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            sut.desactiver(new DeactivatePraCommand(ENTREPOT_ID, ACTEUR_ID));

            verify(regionScopeResolver).verifierAccesRegion(ACTEUR_ID, RegionId.of(REGION_ID));
        }

        @Test
        @DisplayName("type invalide (PNA_CENTRAL) → TypeEntrepotInvalideException, sans vérif de portée")
        void desactiver_typeInvalide_leveException() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(pnaCentraleExistante()));

            var deactivatePraCommand = new DeactivatePraCommand(ENTREPOT_ID, ACTEUR_ID);
            assertThatThrownBy(() -> sut.desactiver(deactivatePraCommand))
                    .isInstanceOf(TypeEntrepotInvalideException.class);

            verifyNoInteractions(regionScopeResolver);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ActivatePraUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ActivatePraUseCaseImpl")
    class ActivatePraTest {

        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @Mock
        RegionScopeResolver regionScopeResolver;
        @Mock
        EntrepotDetailAssembler entrepotDetailAssembler;
        @InjectMocks
        ActivatePraUseCaseImpl sut;

        @Test
        @DisplayName("active la PRA après vérification de la portée régionale")
        void activer_succes_activePra() {
            Entrepot pra = praExistante();
            pra.desactiver();
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(pra));
            when(entrepotRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(entrepotDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            sut.activer(new ActivatePraCommand(ENTREPOT_ID, ACTEUR_ID));

            verify(regionScopeResolver).verifierAccesRegion(ACTEUR_ID, RegionId.of(REGION_ID));
        }

        @Test
        @DisplayName("entrepôt introuvable → EntrepotIntrouvableException")
        void activer_introuvable_leveException() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var activatePraCommand = new ActivatePraCommand(ENTREPOT_ID, ACTEUR_ID);
            assertThatThrownBy(() -> sut.activer(activatePraCommand))
                    .isInstanceOf(EntrepotIntrouvableException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GetEntrepotUseCaseImpl / ListEntrepotsUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("GetEntrepotUseCaseImpl")
    class GetEntrepotTest {

        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @Mock
        EntrepotDetailAssembler entrepotDetailAssembler;
        @InjectMocks
        GetEntrepotUseCaseImpl sut;

        @Test
        @DisplayName("entrepôt trouvé → retourne le détail assemblé")
        void obtenir_succes_retourneDetail() {
            Entrepot pra = praExistante();
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(pra));
            when(entrepotDetailAssembler.assembler(pra)).thenReturn(DETAIL_FICTIF);

            EntrepotDetail result = sut.obtenir(new GetEntrepotQuery(ENTREPOT_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
        }

        @Test
        @DisplayName("entrepôt introuvable → EntrepotIntrouvableException")
        void obtenir_introuvable_leveException() {
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var getEntrepotQuery = new GetEntrepotQuery(ENTREPOT_ID);
            assertThatThrownBy(() -> sut.obtenir(getEntrepotQuery))
                    .isInstanceOf(EntrepotIntrouvableException.class);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ListEntrepotsUseCaseImpl")
    class ListEntrepotsTest {

        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @Mock
        EntrepotDetailAssembler entrepotDetailAssembler;
        @InjectMocks
        ListEntrepotsUseCaseImpl sut;

        @Test
        @DisplayName("construit les critères et la pagination, puis mappe le résultat")
        void lister_succes_mappePage() {
            Entrepot pra = praExistante();
            PageResult<Entrepot> pageResult = PageResult.of(List.of(pra), 0, 20, 1);
            when(entrepotRepositoryPort.search(any(), any())).thenReturn(pageResult);
            when(entrepotDetailAssembler.assembler(pra)).thenReturn(DETAIL_FICTIF);

            EntrepotPage result = sut.lister(
                    new ListEntrepotsQuery(null, TypeEntrepot.PRA, null, null, 0, 20, "nom", "ASC"));

            assertThat(result.content()).containsExactly(DETAIL_FICTIF);
            assertThat(result.totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("aucun résultat → page vide")
        void lister_aucunResultat_pageVide() {
            when(entrepotRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            EntrepotPage result = sut.lister(
                    new ListEntrepotsQuery(null, null, null, null, null, null, null, null));

            assertThat(result.content()).isEmpty();
            verifyNoInteractions(entrepotDetailAssembler);
        }
    }
}
