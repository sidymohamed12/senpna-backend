package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.criteria.AppelOffreSearchCriteria;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListAppelOffresPubliesUseCaseImpl — espace fournisseur, statut forcé à PUBLIE")
class ListAppelOffresPubliesUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    AppelOffreDetailAssembler appelOffreDetailAssembler;

    ListAppelOffresPubliesUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListAppelOffresPubliesUseCaseImpl(appelOffreRepositoryPort, appelOffreDetailAssembler);
    }

    @Test
    @DisplayName("le statut est toujours forcé à PUBLIE, quel que soit le statut demandé par le client")
    void statutToujoursForcePublie() {
        when(appelOffreRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

        // Un statut BROUILLON transmis par erreur (ou malveillance) côté client ne doit
        // jamais transparaître : ListAppelOffresPubliesUseCaseImpl doit l'ignorer.
        sut.lister(new ListAppelOffresQuery("q", StatutAppelOffre.BROUILLON, 0, 20, null, null));

        ArgumentCaptor<AppelOffreSearchCriteria> captor = ArgumentCaptor.forClass(AppelOffreSearchCriteria.class);
        verify(appelOffreRepositoryPort).search(captor.capture(), any(PageRequest.class));
        assertThat(captor.getValue().statut()).isEqualTo(StatutAppelOffre.PUBLIE);
        assertThat(captor.getValue().recherche()).isEqualTo("q");
    }
}
