package ministere.sante.senpna.medicament.application.usecase.famille;

import ministere.sante.senpna.medicament.application.facade.FamilleFacade;
import ministere.sante.senpna.medicament.application.service.FamilleDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.DesarchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.GetFamilleQuery;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ListFamillesQuery;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.UpdateFamilleCommand;
import ministere.sante.senpna.medicament.domain.exception.famille.CodeFamilleDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.in.famille.ArchiveFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.CreateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.DesarchiveFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.GetFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.ListFamillesUseCase;
import ministere.sante.senpna.medicament.domain.port.in.famille.UpdateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.shared.domain.port.out.FamilleCachePort;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Module Famille — use cases, assembler et façade")
class FamilleUseCasesFullTest {

    @Mock
    FamilleRepositoryPort familleRepositoryPort;
    @Mock
    FamilleCachePort familleCachePort;

    FamilleDetailAssembler assembler = new FamilleDetailAssembler();

    @Nested
    @DisplayName("FamilleDetailAssembler")
    class Assembler {

        @Test
        @DisplayName("reporte fidèlement chaque champ")
        void reporteChaqueChamp() {
            Famille famille = Famille.creer("ANTIBIO", "Antibiotiques", "Description");

            FamilleDetail detail = assembler.assembler(famille);

            assertThat(detail.code()).isEqualTo("ANTIBIO");
            assertThat(detail.actif()).isTrue();
        }
    }

    @Nested
    @DisplayName("CreateFamilleUseCaseImpl")
    class Create {

        CreateFamilleUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new CreateFamilleUseCaseImpl(familleRepositoryPort, familleCachePort, assembler);
        }

        @Test
        @DisplayName("code déjà utilisé → CodeFamilleDejaUtiliseException")
        void codeDejaUtilise_leveException() {
            when(familleRepositoryPort.existsByCodeIgnoreCase("ANTIBIO")).thenReturn(true);

            var command = new CreateFamilleCommand("ANTIBIO", "Antibiotiques", null);
            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(CodeFamilleDejaUtiliseException.class);

            verify(familleCachePort, never()).reload();
        }

        @Test
        @DisplayName("création réussie → sauvegarde et recharge le cache")
        void creationReussie_sauvegardeEtRechargeCache() {
            when(familleRepositoryPort.existsByCodeIgnoreCase(any())).thenReturn(false);
            when(familleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var command = new CreateFamilleCommand("ANTIBIO", "Antibiotiques", null);
            sut.creer(command);

            verify(familleCachePort).reload();
        }
    }

    @Nested
    @DisplayName("UpdateFamilleUseCaseImpl / ArchiveFamilleUseCaseImpl / DesarchiveFamilleUseCaseImpl")
    class UpdateArchiveDesarchive {

        UUID id;
        Famille famille;

        @BeforeEach
        void setUp() {
            id = UUID.randomUUID();
            famille = Famille.creer("ANTIBIO", "Antibiotiques", null);
        }

        @Test
        @DisplayName("modifier() met à jour libellé/description et recharge le cache")
        void modifier_metAJourEtRechargeCache() {
            when(familleRepositoryPort.findById(FamilleId.of(id))).thenReturn(Optional.of(famille));
            when(familleRepositoryPort.save(famille)).thenReturn(famille);

            new UpdateFamilleUseCaseImpl(familleRepositoryPort, familleCachePort, assembler)
                    .modifier(new UpdateFamilleCommand(id, "Nouveau libellé", "Nouvelle desc"));

            assertThat(famille.getLibelle()).isEqualTo("Nouveau libellé");
            verify(familleCachePort).reload();
        }

        @Test
        @DisplayName("archiver() désactive la famille")
        void archiver_desactive() {
            when(familleRepositoryPort.findById(FamilleId.of(id))).thenReturn(Optional.of(famille));
            when(familleRepositoryPort.save(famille)).thenReturn(famille);

            new ArchiveFamilleUseCaseImpl(familleRepositoryPort, familleCachePort, assembler)
                    .archiver(new ArchiveFamilleCommand(id));

            assertThat(famille.isActif()).isFalse();
        }

        @Test
        @DisplayName("desarchiver() réactive la famille")
        void desarchiver_reactive() {
            famille.archiver();
            when(familleRepositoryPort.findById(FamilleId.of(id))).thenReturn(Optional.of(famille));
            when(familleRepositoryPort.save(famille)).thenReturn(famille);

            new DesarchiveFamilleUseCaseImpl(familleRepositoryPort, familleCachePort, assembler)
                    .desarchiver(new DesarchiveFamilleCommand(id));

            assertThat(famille.isActif()).isTrue();
        }

        @Test
        @DisplayName("introuvable (modifier) → FamilleIntrouvableException")
        void introuvable_leveException() {
            when(familleRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var sut = new UpdateFamilleUseCaseImpl(familleRepositoryPort, familleCachePort, assembler);
            var command = new UpdateFamilleCommand(UUID.randomUUID(), "L", "D");
            assertThatThrownBy(() -> sut
                    .modifier(command))
                    .isInstanceOf(FamilleIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("GetFamilleUseCaseImpl / ListFamillesUseCaseImpl")
    class GetEtList {

        @Test
        @DisplayName("obtenir() introuvable → FamilleIntrouvableException")
        void obtenir_introuvable_leveException() {
            when(familleRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var sut = new GetFamilleUseCaseImpl(familleRepositoryPort, assembler);
            var command = new GetFamilleQuery(UUID.randomUUID());
            assertThatThrownBy(() -> sut
                    .obtenir(command))
                    .isInstanceOf(FamilleIntrouvableException.class);
        }

        @Test
        @DisplayName("lister() aucun résultat → page vide")
        void lister_aucunResultat_pageVide() {
            when(familleRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            FamillePage page = new ListFamillesUseCaseImpl(familleRepositoryPort, assembler)
                    .lister(new ListFamillesQuery(null, null, 0, 20, null, null));

            assertThat(page.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("FamilleFacade")
    class Facade {

        @Mock
        CreateFamilleUseCase createFamilleUseCase;
        @Mock
        UpdateFamilleUseCase updateFamilleUseCase;
        @Mock
        ArchiveFamilleUseCase archiveFamilleUseCase;
        @Mock
        DesarchiveFamilleUseCase desarchiveFamilleUseCase;
        @Mock
        GetFamilleUseCase getFamilleUseCase;
        @Mock
        ListFamillesUseCase listFamillesUseCase;

        @Test
        @DisplayName("chaque méthode délègue au use case correspondant")
        void chaqueMethodeDelegue() {
            FamilleFacade sut = new FamilleFacade(createFamilleUseCase, updateFamilleUseCase, archiveFamilleUseCase,
                    desarchiveFamilleUseCase, getFamilleUseCase, listFamillesUseCase);
            UUID id = UUID.randomUUID();
            FamilleDetail detail = new FamilleDetail(id, "C", "L", null, true, null, null);
            FamillePage page = new FamillePage(List.of(), 0, 20, 0, 0);

            when(createFamilleUseCase.creer(any())).thenReturn(detail);
            when(updateFamilleUseCase.modifier(any())).thenReturn(detail);
            when(archiveFamilleUseCase.archiver(any())).thenReturn(detail);
            when(desarchiveFamilleUseCase.desarchiver(any())).thenReturn(detail);
            when(getFamilleUseCase.obtenir(any())).thenReturn(detail);
            when(listFamillesUseCase.lister(any())).thenReturn(page);

            assertThat(sut.creerFamille(new CreateFamilleCommand("C", "L", null))).isSameAs(detail);
            assertThat(sut.modifierFamille(new UpdateFamilleCommand(id, "L", null))).isSameAs(detail);
            assertThat(sut.archiverFamille(new ArchiveFamilleCommand(id))).isSameAs(detail);
            assertThat(sut.desarchiverFamille(new DesarchiveFamilleCommand(id))).isSameAs(detail);
            assertThat(sut.obtenirFamille(new GetFamilleQuery(id))).isSameAs(detail);
            assertThat(sut.listerFamilles(new ListFamillesQuery(null, null, null, null, null, null)))
                    .isSameAs(page);
        }
    }
}
