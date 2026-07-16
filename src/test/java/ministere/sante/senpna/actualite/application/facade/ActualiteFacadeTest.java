package ministere.sante.senpna.actualite.application.facade;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualitePage;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.CreateActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.DesactiverActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.GetActualiteQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ListActualitesQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.PublierActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.RemettreEnBrouillonActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.UpdateActualiteCommand;
import ministere.sante.senpna.actualite.domain.port.in.CreateActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.DesactiverActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.GetActualitePubliqueUseCase;
import ministere.sante.senpna.actualite.domain.port.in.GetActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.ListActualitesUseCase;
import ministere.sante.senpna.actualite.domain.port.in.PublierActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.RemettreEnBrouillonActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.in.UpdateActualiteUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActualiteFacade — délégation vers les use cases")
class ActualiteFacadeTest {

    @Mock
    private CreateActualiteUseCase createActualiteUseCase;
    @Mock
    private UpdateActualiteUseCase updateActualiteUseCase;
    @Mock
    private PublierActualiteUseCase publierActualiteUseCase;
    @Mock
    private DesactiverActualiteUseCase desactiverActualiteUseCase;
    @Mock
    private RemettreEnBrouillonActualiteUseCase remettreEnBrouillonActualiteUseCase;
    @Mock
    private GetActualiteUseCase getActualiteUseCase;
    @Mock
    private GetActualitePubliqueUseCase getActualitePubliqueUseCase;
    @Mock
    private ListActualitesUseCase listActualitesUseCase;

    private ActualiteFacade facade;

    private static final UUID ACTUALITE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        facade = new ActualiteFacade(createActualiteUseCase, updateActualiteUseCase, publierActualiteUseCase,
                desactiverActualiteUseCase, remettreEnBrouillonActualiteUseCase, getActualiteUseCase,
                getActualitePubliqueUseCase, listActualitesUseCase);
    }

    @Test
    @DisplayName("creerActualite() délègue à CreateActualiteUseCase")
    void creerActualite_delegue() {
        CreateActualiteCommand command = new CreateActualiteCommand(UUID.randomUUID(), "PROJET", "Titre", null,
                List.of(), List.of());
        ActualiteDetail detail = detail();
        when(createActualiteUseCase.creer(command)).thenReturn(detail);

        ActualiteDetail result = facade.creerActualite(command);

        assertThat(result).isEqualTo(detail);
        verify(createActualiteUseCase).creer(command);
    }

    @Test
    @DisplayName("modifierActualite() délègue à UpdateActualiteUseCase")
    void modifierActualite_delegue() {
        UpdateActualiteCommand command = new UpdateActualiteCommand(ACTUALITE_ID, "PROJET", "Titre", null,
                List.of(), List.of());
        ActualiteDetail detail = detail();
        when(updateActualiteUseCase.modifier(command)).thenReturn(detail);

        ActualiteDetail result = facade.modifierActualite(command);

        assertThat(result).isEqualTo(detail);
        verify(updateActualiteUseCase).modifier(command);
    }

    @Test
    @DisplayName("publierActualite() délègue à PublierActualiteUseCase")
    void publierActualite_delegue() {
        PublierActualiteCommand command = new PublierActualiteCommand(ACTUALITE_ID);
        ActualiteDetail detail = detail();
        when(publierActualiteUseCase.publier(command)).thenReturn(detail);

        ActualiteDetail result = facade.publierActualite(command);

        assertThat(result).isEqualTo(detail);
        verify(publierActualiteUseCase).publier(command);
    }

    @Test
    @DisplayName("desactiverActualite() délègue à DesactiverActualiteUseCase")
    void desactiverActualite_delegue() {
        DesactiverActualiteCommand command = new DesactiverActualiteCommand(ACTUALITE_ID);
        ActualiteDetail detail = detail();
        when(desactiverActualiteUseCase.desactiver(command)).thenReturn(detail);

        ActualiteDetail result = facade.desactiverActualite(command);

        assertThat(result).isEqualTo(detail);
        verify(desactiverActualiteUseCase).desactiver(command);
    }

    @Test
    @DisplayName("remettreEnBrouillonActualite() délègue à RemettreEnBrouillonActualiteUseCase")
    void remettreEnBrouillonActualite_delegue() {
        RemettreEnBrouillonActualiteCommand command = new RemettreEnBrouillonActualiteCommand(ACTUALITE_ID);
        ActualiteDetail detail = detail();
        when(remettreEnBrouillonActualiteUseCase.remettreEnBrouillon(command)).thenReturn(detail);

        ActualiteDetail result = facade.remettreEnBrouillonActualite(command);

        assertThat(result).isEqualTo(detail);
        verify(remettreEnBrouillonActualiteUseCase).remettreEnBrouillon(command);
    }

    @Test
    @DisplayName("obtenirActualite() délègue à GetActualiteUseCase")
    void obtenirActualite_delegue() {
        GetActualiteQuery query = new GetActualiteQuery(ACTUALITE_ID);
        ActualiteDetail detail = detail();
        when(getActualiteUseCase.obtenir(query)).thenReturn(detail);

        ActualiteDetail result = facade.obtenirActualite(query);

        assertThat(result).isEqualTo(detail);
        verify(getActualiteUseCase).obtenir(query);
    }

    @Test
    @DisplayName("obtenirActualitePublique() délègue à GetActualitePubliqueUseCase")
    void obtenirActualitePublique_delegue() {
        GetActualiteQuery query = new GetActualiteQuery(ACTUALITE_ID);
        ActualiteDetail detail = detail();
        when(getActualitePubliqueUseCase.obtenirPublique(query)).thenReturn(detail);

        ActualiteDetail result = facade.obtenirActualitePublique(query);

        assertThat(result).isEqualTo(detail);
        verify(getActualitePubliqueUseCase).obtenirPublique(query);
    }

    @Test
    @DisplayName("listerActualites() délègue à ListActualitesUseCase")
    void listerActualites_delegue() {
        ListActualitesQuery query = new ListActualitesQuery("q", "PROJET", "PUBLIE", 0, 20, "createdAt", "DESC");
        ActualitePage page = new ActualitePage(List.of(detail()), 0, 20, 1, 1);
        when(listActualitesUseCase.lister(query)).thenReturn(page);

        ActualitePage result = facade.listerActualites(query);

        assertThat(result).isEqualTo(page);
        verify(listActualitesUseCase).lister(query);
    }

    private ActualiteDetail detail() {
        return new ActualiteDetail(ACTUALITE_ID, "PROJET", "Titre", null, List.of(), UUID.randomUUID(),
                "Awa Diop", List.of(), "BROUILLON", null, null);
    }
}
