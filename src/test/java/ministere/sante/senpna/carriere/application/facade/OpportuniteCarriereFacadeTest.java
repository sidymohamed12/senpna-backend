package ministere.sante.senpna.carriere.application.facade;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CloturerOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CreateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.ListOpportunitesCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.MettreEnCoursOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarrierePage;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.PublierOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.RemettreEnBrouillonOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.UpdateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.port.in.CloturerOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.CreateOpportuniteCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.in.GetOpportuniteCarrierePubliqueUseCase;
import ministere.sante.senpna.carriere.domain.port.in.GetOpportuniteCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.in.ListOpportunitesCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.in.MettreEnCoursOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.PublierOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.RemettreEnBrouillonOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.in.UpdateOpportuniteCarriereUseCase;

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
@DisplayName("OpportuniteCarriereFacade — délégation aux use cases")
class OpportuniteCarriereFacadeTest {

    @Mock
    CreateOpportuniteCarriereUseCase createOpportuniteCarriereUseCase;
    @Mock
    UpdateOpportuniteCarriereUseCase updateOpportuniteCarriereUseCase;
    @Mock
    PublierOpportuniteUseCase publierOpportuniteUseCase;
    @Mock
    MettreEnCoursOpportuniteUseCase mettreEnCoursOpportuniteUseCase;
    @Mock
    CloturerOpportuniteUseCase cloturerOpportuniteUseCase;
    @Mock
    RemettreEnBrouillonOpportuniteUseCase remettreEnBrouillonOpportuniteUseCase;
    @Mock
    GetOpportuniteCarriereUseCase getOpportuniteCarriereUseCase;
    @Mock
    GetOpportuniteCarrierePubliqueUseCase getOpportuniteCarrierePubliqueUseCase;
    @Mock
    ListOpportunitesCarriereUseCase listOpportunitesCarriereUseCase;

    OpportuniteCarriereFacade sut;

    @BeforeEach
    void setUp() {
        sut = new OpportuniteCarriereFacade(createOpportuniteCarriereUseCase, updateOpportuniteCarriereUseCase,
                publierOpportuniteUseCase, mettreEnCoursOpportuniteUseCase, cloturerOpportuniteUseCase,
                remettreEnBrouillonOpportuniteUseCase, getOpportuniteCarriereUseCase,
                getOpportuniteCarrierePubliqueUseCase, listOpportunitesCarriereUseCase);
    }

    @Test
    @DisplayName("chaque méthode de façade délègue au use case correspondant")
    void chaqueMethodeDelegue() {
        UUID id = UUID.randomUUID();
        OpportuniteCarriereDetail detail = new OpportuniteCarriereDetail(id, "T", "E", null, null, "L", "CDI", null,
                null, null, null, null, "BROUILLON", null, null);
        OpportuniteCarrierePage page = new OpportuniteCarrierePage(java.util.List.of(), 0, 20, 0, 0);

        when(createOpportuniteCarriereUseCase.creer(any())).thenReturn(detail);
        when(updateOpportuniteCarriereUseCase.modifier(any())).thenReturn(detail);
        when(publierOpportuniteUseCase.publier(any())).thenReturn(detail);
        when(mettreEnCoursOpportuniteUseCase.mettreEnCours(any())).thenReturn(detail);
        when(cloturerOpportuniteUseCase.cloturer(any())).thenReturn(detail);
        when(remettreEnBrouillonOpportuniteUseCase.remettreEnBrouillon(any())).thenReturn(detail);
        when(getOpportuniteCarriereUseCase.obtenir(any())).thenReturn(detail);
        when(getOpportuniteCarrierePubliqueUseCase.obtenirPublique(any())).thenReturn(detail);
        when(listOpportunitesCarriereUseCase.lister(any())).thenReturn(page);

        assertThat(sut.creerOpportunite(new CreateOpportuniteCarriereCommand(id, "T", "E", "D", null, "L", "CDI",
                null, null, null))).isSameAs(detail);
        assertThat(sut.modifierOpportunite(new UpdateOpportuniteCarriereCommand(id, "T", "E", "D", null, "L", "CDI",
                null, null, null))).isSameAs(detail);
        assertThat(sut.publierOpportunite(new PublierOpportuniteCommand(id))).isSameAs(detail);
        assertThat(sut.mettreEnCoursOpportunite(new MettreEnCoursOpportuniteCommand(id))).isSameAs(detail);
        assertThat(sut.cloturerOpportunite(new CloturerOpportuniteCommand(id))).isSameAs(detail);
        assertThat(sut.remettreEnBrouillonOpportunite(new RemettreEnBrouillonOpportuniteCommand(id)))
                .isSameAs(detail);
        assertThat(sut.obtenirOpportunite(new GetOpportuniteCarriereQuery(id))).isSameAs(detail);
        assertThat(sut.obtenirOpportunitePublique(new GetOpportuniteCarriereQuery(id))).isSameAs(detail);
        assertThat(sut.listerOpportunites(
                new ListOpportunitesCarriereQuery(null, null, null, false, null, null, null, null)))
                .isSameAs(page);
    }
}
