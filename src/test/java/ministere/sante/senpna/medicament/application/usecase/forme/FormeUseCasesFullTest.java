package ministere.sante.senpna.medicament.application.usecase.forme;

import ministere.sante.senpna.medicament.application.facade.FormeFacade;
import ministere.sante.senpna.medicament.application.service.FormeDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.ArchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.CreateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.DesarchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormePage;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.GetFormeQuery;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.ListFormesQuery;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.UpdateFormeCommand;
import ministere.sante.senpna.medicament.domain.exception.forme.CodeFormeDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.port.in.forme.ArchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.CreateFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.DesarchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.GetFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.ListFormesUseCase;
import ministere.sante.senpna.medicament.domain.port.in.forme.UpdateFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.shared.domain.port.out.FormeCachePort;
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
@DisplayName("Module Forme — use cases, assembler et façade")
class FormeUseCasesFullTest {

    @Mock
    FormeRepositoryPort formeRepositoryPort;
    @Mock
    FormeCachePort formeCachePort;

    FormeDetailAssembler assembler = new FormeDetailAssembler();

    @Nested
    @DisplayName("FormeDetailAssembler")
    class Assembler {

        @Test
        @DisplayName("reporte fidèlement chaque champ")
        void reporteChaqueChamp() {
            Forme forme = Forme.creer("COMP", "Comprimé", "Description");

            FormeDetail detail = assembler.assembler(forme);

            assertThat(detail.code()).isEqualTo("COMP");
            assertThat(detail.actif()).isTrue();
        }
    }

    @Nested
    @DisplayName("CreateFormeUseCaseImpl")
    class Create {

        CreateFormeUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new CreateFormeUseCaseImpl(formeRepositoryPort, formeCachePort, assembler);
        }

        @Test
        @DisplayName("code déjà utilisé → CodeFormeDejaUtiliseException")
        void codeDejaUtilise_leveException() {
            when(formeRepositoryPort.existsByCodeIgnoreCase("COMP")).thenReturn(true);

            var command = new CreateFormeCommand("COMP", "Comprimé", null);
            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(CodeFormeDejaUtiliseException.class);

            verify(formeCachePort, never()).reload();
        }

        @Test
        @DisplayName("création réussie → sauvegarde et recharge le cache")
        void creationReussie_sauvegardeEtRechargeCache() {
            when(formeRepositoryPort.existsByCodeIgnoreCase(any())).thenReturn(false);
            when(formeRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            sut.creer(new CreateFormeCommand("COMP", "Comprimé", null));

            verify(formeCachePort).reload();
        }
    }

    @Nested
    @DisplayName("UpdateFormeUseCaseImpl / ArchiveFormeUseCaseImpl / DesarchiveFormeUseCaseImpl")
    class UpdateArchiveDesarchive {

        UUID id;
        Forme forme;

        @BeforeEach
        void setUp() {
            id = UUID.randomUUID();
            forme = Forme.creer("COMP", "Comprimé", null);
        }

        @Test
        @DisplayName("modifier() met à jour libellé/description et recharge le cache")
        void modifier_metAJourEtRechargeCache() {
            when(formeRepositoryPort.findById(FormeId.of(id))).thenReturn(Optional.of(forme));
            when(formeRepositoryPort.save(forme)).thenReturn(forme);

            new UpdateFormeUseCaseImpl(formeRepositoryPort, formeCachePort, assembler)
                    .modifier(new UpdateFormeCommand(id, "Nouveau libellé", "Nouvelle desc"));

            assertThat(forme.getLibelle()).isEqualTo("Nouveau libellé");
            verify(formeCachePort).reload();
        }

        @Test
        @DisplayName("archiver() désactive la forme")
        void archiver_desactive() {
            when(formeRepositoryPort.findById(FormeId.of(id))).thenReturn(Optional.of(forme));
            when(formeRepositoryPort.save(forme)).thenReturn(forme);

            new ArchiveFormeUseCaseImpl(formeRepositoryPort, formeCachePort, assembler)
                    .archiver(new ArchiveFormeCommand(id));

            assertThat(forme.isActif()).isFalse();
        }

        @Test
        @DisplayName("desarchiver() réactive la forme")
        void desarchiver_reactive() {
            forme.archiver();
            when(formeRepositoryPort.findById(FormeId.of(id))).thenReturn(Optional.of(forme));
            when(formeRepositoryPort.save(forme)).thenReturn(forme);

            new DesarchiveFormeUseCaseImpl(formeRepositoryPort, formeCachePort, assembler)
                    .desarchiver(new DesarchiveFormeCommand(id));

            assertThat(forme.isActif()).isTrue();
        }

        @Test
        @DisplayName("introuvable (modifier) → FormeIntrouvableException")
        void introuvable_leveException() {
            when(formeRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var sut = new UpdateFormeUseCaseImpl(formeRepositoryPort, formeCachePort, assembler);
            var command = new UpdateFormeCommand(UUID.randomUUID(), "L", "D");
            assertThatThrownBy(() -> sut
                    .modifier(command))
                    .isInstanceOf(FormeIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("GetFormeUseCaseImpl / ListFormesUseCaseImpl")
    class GetEtList {

        @Test
        @DisplayName("obtenir() introuvable → FormeIntrouvableException")
        void obtenir_introuvable_leveException() {
            when(formeRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var sut = new GetFormeUseCaseImpl(formeRepositoryPort, assembler);
            var command = new GetFormeQuery(UUID.randomUUID());
            assertThatThrownBy(() -> sut
                    .obtenir(command))
                    .isInstanceOf(FormeIntrouvableException.class);
        }

        @Test
        @DisplayName("lister() aucun résultat → page vide")
        void lister_aucunResultat_pageVide() {
            when(formeRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            FormePage page = new ListFormesUseCaseImpl(formeRepositoryPort, assembler)
                    .lister(new ListFormesQuery(null, null, 0, 20, null, null));

            assertThat(page.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("FormeFacade")
    class Facade {

        @Mock
        CreateFormeUseCase createFormeUseCase;
        @Mock
        UpdateFormeUseCase updateFormeUseCase;
        @Mock
        ArchiveFormeUseCase archiveFormeUseCase;
        @Mock
        DesarchiveFormeUseCase desarchiveFormeUseCase;
        @Mock
        GetFormeUseCase getFormeUseCase;
        @Mock
        ListFormesUseCase listFormesUseCase;

        @Test
        @DisplayName("chaque méthode délègue au use case correspondant")
        void chaqueMethodeDelegue() {
            FormeFacade sut = new FormeFacade(createFormeUseCase, updateFormeUseCase, archiveFormeUseCase,
                    desarchiveFormeUseCase, getFormeUseCase, listFormesUseCase);
            UUID id = UUID.randomUUID();
            FormeDetail detail = new FormeDetail(id, "C", "L", null, true, null, null);
            FormePage page = new FormePage(List.of(), 0, 20, 0, 0);

            when(createFormeUseCase.creer(any())).thenReturn(detail);
            when(updateFormeUseCase.modifier(any())).thenReturn(detail);
            when(archiveFormeUseCase.archiver(any())).thenReturn(detail);
            when(desarchiveFormeUseCase.desarchiver(any())).thenReturn(detail);
            when(getFormeUseCase.obtenir(any())).thenReturn(detail);
            when(listFormesUseCase.lister(any())).thenReturn(page);

            assertThat(sut.creerForme(new CreateFormeCommand("C", "L", null))).isSameAs(detail);
            assertThat(sut.modifierForme(new UpdateFormeCommand(id, "L", null))).isSameAs(detail);
            assertThat(sut.archiverForme(new ArchiveFormeCommand(id))).isSameAs(detail);
            assertThat(sut.desarchiverForme(new DesarchiveFormeCommand(id))).isSameAs(detail);
            assertThat(sut.obtenirForme(new GetFormeQuery(id))).isSameAs(detail);
            assertThat(sut.listerFormes(new ListFormesQuery(null, null, null, null, null, null))).isSameAs(page);
        }
    }
}
