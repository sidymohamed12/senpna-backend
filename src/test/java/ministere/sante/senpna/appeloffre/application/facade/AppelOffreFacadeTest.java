package ministere.sante.senpna.appeloffre.application.facade;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AnnulerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AttribuerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ClorerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.CreateAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.PublierAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListOffresAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RejeterOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetenirOffreCommand;
import ministere.sante.senpna.appeloffre.domain.port.in.AnnulerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.AttribuerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ClorerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.CreateAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.GetAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListAppelOffresUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListOffresAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.PublierAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.RejeterOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.RetenirOffreUseCase;

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
@DisplayName("AppelOffreFacade — délégation aux use cases (PNA)")
class AppelOffreFacadeTest {

        @Mock
        CreateAppelOffreUseCase createAppelOffreUseCase;
        @Mock
        PublierAppelOffreUseCase publierAppelOffreUseCase;
        @Mock
        ClorerAppelOffreUseCase clorerAppelOffreUseCase;
        @Mock
        AnnulerAppelOffreUseCase annulerAppelOffreUseCase;
        @Mock
        AttribuerAppelOffreUseCase attribuerAppelOffreUseCase;
        @Mock
        GetAppelOffreUseCase getAppelOffreUseCase;
        @Mock
        ListAppelOffresUseCase listAppelOffresUseCase;
        @Mock
        ListOffresAppelOffreUseCase listOffresAppelOffreUseCase;
        @Mock
        RetenirOffreUseCase retenirOffreUseCase;
        @Mock
        RejeterOffreUseCase rejeterOffreUseCase;

        AppelOffreFacade sut;

        @BeforeEach
        void setUp() {
                sut = new AppelOffreFacade(createAppelOffreUseCase, publierAppelOffreUseCase, clorerAppelOffreUseCase,
                                annulerAppelOffreUseCase, attribuerAppelOffreUseCase, getAppelOffreUseCase,
                                listAppelOffresUseCase,
                                listOffresAppelOffreUseCase, retenirOffreUseCase, rejeterOffreUseCase);
        }

        @Test
        @DisplayName("chaque méthode de façade délègue au use case correspondant")
        void chaqueMethodeDelegue() {
                UUID id = UUID.randomUUID();
                AppelOffreDetail appelOffreDetail = new AppelOffreDetail(id, "AO-1", "Objet",
                                LocalDate.now().plusDays(10),
                                null, List.of(), null, null);
                AppelOffrePage appelOffrePage = new AppelOffrePage(List.of(), 0, 20, 0, 0);
                OffreDetail offreDetail = new OffreDetail(id, id, id, null, null, List.of(), null, null);
                OffrePage offrePage = new OffrePage(List.of(), 0, 20, 0, 0);

                when(createAppelOffreUseCase.creer(any())).thenReturn(appelOffreDetail);
                when(publierAppelOffreUseCase.publier(any())).thenReturn(appelOffreDetail);
                when(clorerAppelOffreUseCase.clorer(any())).thenReturn(appelOffreDetail);
                when(annulerAppelOffreUseCase.annuler(any())).thenReturn(appelOffreDetail);
                when(attribuerAppelOffreUseCase.attribuer(any())).thenReturn(appelOffreDetail);
                when(getAppelOffreUseCase.obtenir(any())).thenReturn(appelOffreDetail);
                when(listAppelOffresUseCase.lister(any())).thenReturn(appelOffrePage);
                when(listOffresAppelOffreUseCase.lister(any())).thenReturn(offrePage);
                when(retenirOffreUseCase.retenir(any())).thenReturn(offreDetail);
                when(rejeterOffreUseCase.rejeter(any())).thenReturn(offreDetail);

                assertThat(sut.creer(
                                new CreateAppelOffreCommand("AO-1", "Objet", LocalDate.now().plusDays(10), List.of())))
                                .isSameAs(appelOffreDetail);
                assertThat(sut.publier(new PublierAppelOffreCommand(id))).isSameAs(appelOffreDetail);
                assertThat(sut.clorer(new ClorerAppelOffreCommand(id))).isSameAs(appelOffreDetail);
                assertThat(sut.annuler(new AnnulerAppelOffreCommand(id))).isSameAs(appelOffreDetail);
                assertThat(sut.attribuer(new AttribuerAppelOffreCommand(id, List.of(), List.of())))
                                .isSameAs(appelOffreDetail);
                assertThat(sut.obtenir(new GetAppelOffreQuery(id))).isSameAs(appelOffreDetail);
                assertThat(sut.lister(new ListAppelOffresQuery(null, null, 0, 20, null, null)))
                                .isSameAs(appelOffrePage);
                assertThat(sut.listerOffres(new ListOffresAppelOffreQuery(id, 0, 20))).isSameAs(offrePage);
                assertThat(sut.retenirOffre(new RetenirOffreCommand(id))).isSameAs(offreDetail);
                assertThat(sut.rejeterOffre(new RejeterOffreCommand(id))).isSameAs(offreDetail);
        }
}
