package ministere.sante.senpna.appeloffre.application.facade;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.GetOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListMesOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetirerOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.SoumettreOffreCommand;
import ministere.sante.senpna.appeloffre.domain.port.in.GetAppelOffrePublieUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.GetOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListAppelOffresPubliesUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListMesOffresUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.RetirerOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.SoumettreOffreUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EspaceFournisseurAppelOffreFacade — délégation aux use cases (espace fournisseur)")
class EspaceFournisseurAppelOffreFacadeTest {

    @Mock
    ListAppelOffresPubliesUseCase listAppelOffresPubliesUseCase;
    @Mock
    GetAppelOffrePublieUseCase getAppelOffrePublieUseCase;
    @Mock
    SoumettreOffreUseCase soumettreOffreUseCase;
    @Mock
    RetirerOffreUseCase retirerOffreUseCase;
    @Mock
    GetOffreUseCase getOffreUseCase;
    @Mock
    ListMesOffresUseCase listMesOffresUseCase;

    EspaceFournisseurAppelOffreFacade sut;

    @BeforeEach
    void setUp() {
        sut = new EspaceFournisseurAppelOffreFacade(listAppelOffresPubliesUseCase, getAppelOffrePublieUseCase,
                soumettreOffreUseCase, retirerOffreUseCase, getOffreUseCase, listMesOffresUseCase);
    }

    @Test
    @DisplayName("chaque méthode de façade délègue au use case correspondant")
    void chaqueMethodeDelegue() {
        UUID id = UUID.randomUUID();
        AppelOffreDetail appelOffreDetail = new AppelOffreDetail(id, "AO-1", "Objet", LocalDate.now().plusDays(10),
                null, List.of(), null, null);
        AppelOffrePage appelOffrePage = new AppelOffrePage(List.of(), 0, 20, 0, 0);
        OffreDetail offreDetail = new OffreDetail(id, id, id, null, null, List.of(), null, null);
        OffrePage offrePage = new OffrePage(List.of(), 0, 20, 0, 0);

        when(listAppelOffresPubliesUseCase.lister(any())).thenReturn(appelOffrePage);
        when(getAppelOffrePublieUseCase.obtenir(any())).thenReturn(appelOffreDetail);
        when(soumettreOffreUseCase.soumettre(any())).thenReturn(offreDetail);
        when(retirerOffreUseCase.retirer(any())).thenReturn(offreDetail);
        when(getOffreUseCase.obtenir(any())).thenReturn(offreDetail);
        when(listMesOffresUseCase.lister(any())).thenReturn(offrePage);

        assertThat(sut.listerAppelsOffresPublies(new ListAppelOffresQuery(null, null, 0, 20, null, null)))
                .isSameAs(appelOffrePage);
        assertThat(sut.obtenirAppelOffre(new GetAppelOffreQuery(id))).isSameAs(appelOffreDetail);
        assertThat(sut.soumettreOffre(new SoumettreOffreCommand(id, id, null, List.of()))).isSameAs(offreDetail);
        assertThat(sut.retirerOffre(new RetirerOffreCommand(id, id))).isSameAs(offreDetail);
        assertThat(sut.obtenirOffre(new GetOffreQuery(id, id))).isSameAs(offreDetail);
        assertThat(sut.listerMesOffres(new ListMesOffresQuery(id, null, 0, 20))).isSameAs(offrePage);
    }
}
