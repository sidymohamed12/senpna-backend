package ministere.sante.senpna.projet.application.facade;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ArchiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.CreateProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.DesactiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.GetProjetQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ListProjetsQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetPage;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.PublierProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.RemettreEnBrouillonProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.UpdateProjetCommand;
import ministere.sante.senpna.projet.domain.port.in.ArchiverProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.CreateProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.DesactiverProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.GetProjetPubliqueUseCase;
import ministere.sante.senpna.projet.domain.port.in.GetProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.ListProjetsUseCase;
import ministere.sante.senpna.projet.domain.port.in.PublierProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.RemettreEnBrouillonProjetUseCase;
import ministere.sante.senpna.projet.domain.port.in.UpdateProjetUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjetFacade — délégation aux use cases")
class ProjetFacadeTest {

    @Mock
    CreateProjetUseCase createProjetUseCase;
    @Mock
    UpdateProjetUseCase updateProjetUseCase;
    @Mock
    PublierProjetUseCase publierProjetUseCase;
    @Mock
    ArchiverProjetUseCase archiverProjetUseCase;
    @Mock
    DesactiverProjetUseCase desactiverProjetUseCase;
    @Mock
    RemettreEnBrouillonProjetUseCase remettreEnBrouillonProjetUseCase;
    @Mock
    GetProjetUseCase getProjetUseCase;
    @Mock
    GetProjetPubliqueUseCase getProjetPubliqueUseCase;
    @Mock
    ListProjetsUseCase listProjetsUseCase;

    ProjetFacade sut;

    @BeforeEach
    void setUp() {
        sut = new ProjetFacade(createProjetUseCase, updateProjetUseCase, publierProjetUseCase,
                archiverProjetUseCase, desactiverProjetUseCase, remettreEnBrouillonProjetUseCase, getProjetUseCase,
                getProjetPubliqueUseCase, listProjetsUseCase);
    }

    @Test
    @DisplayName("chaque méthode de façade délègue au use case correspondant")
    void chaqueMethodeDelegue() {
        UUID id = UUID.randomUUID();
        ProjetDetail detail = new ProjetDetail(id, "SANTE", "N", null, null, null, null, "BROUILLON", null, null);
        ProjetPage page = new ProjetPage(java.util.List.of(), 0, 20, 0, 0);

        when(createProjetUseCase.creer(any())).thenReturn(detail);
        when(updateProjetUseCase.modifier(any())).thenReturn(detail);
        when(publierProjetUseCase.publier(any())).thenReturn(detail);
        when(archiverProjetUseCase.archiver(any())).thenReturn(detail);
        when(desactiverProjetUseCase.desactiver(any())).thenReturn(detail);
        when(remettreEnBrouillonProjetUseCase.remettreEnBrouillon(any())).thenReturn(detail);
        when(getProjetUseCase.obtenir(any())).thenReturn(detail);
        when(getProjetPubliqueUseCase.obtenirPublique(any())).thenReturn(detail);
        when(listProjetsUseCase.lister(any())).thenReturn(page);

        assertThat(sut.creerProjet(new CreateProjetCommand("SANTE", "N", null, null, null, null))).isSameAs(detail);
        assertThat(sut.modifierProjet(new UpdateProjetCommand(id, "SANTE", "N", null, null, null, null)))
                .isSameAs(detail);
        assertThat(sut.publierProjet(new PublierProjetCommand(id))).isSameAs(detail);
        assertThat(sut.archiverProjet(new ArchiverProjetCommand(id))).isSameAs(detail);
        assertThat(sut.desactiverProjet(new DesactiverProjetCommand(id))).isSameAs(detail);
        assertThat(sut.remettreEnBrouillonProjet(new RemettreEnBrouillonProjetCommand(id))).isSameAs(detail);
        assertThat(sut.obtenirProjet(new GetProjetQuery(id))).isSameAs(detail);
        assertThat(sut.obtenirProjetPublique(new GetProjetQuery(id))).isSameAs(detail);
        assertThat(sut.listerProjets(new ListProjetsQuery(null, null, null, null, null, null, null)))
                .isSameAs(page);
    }
}
