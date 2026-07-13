package ministere.sante.senpna.carriere.application.facade;

import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidaturePage;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.GetCandidatureQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.ListCandidaturesQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.SoumettreCandidatureCommand;
import ministere.sante.senpna.carriere.domain.port.in.GetCandidatureUseCase;
import ministere.sante.senpna.carriere.domain.port.in.ListCandidaturesUseCase;
import ministere.sante.senpna.carriere.domain.port.in.SoumettreCandidatureUseCase;

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
@DisplayName("CandidatureFacade — délégation aux use cases")
class CandidatureFacadeTest {

    @Mock
    SoumettreCandidatureUseCase soumettreCandidatureUseCase;
    @Mock
    GetCandidatureUseCase getCandidatureUseCase;
    @Mock
    ListCandidaturesUseCase listCandidaturesUseCase;

    CandidatureFacade sut;

    @BeforeEach
    void setUp() {
        sut = new CandidatureFacade(soumettreCandidatureUseCase, getCandidatureUseCase, listCandidaturesUseCase);
    }

    @Test
    @DisplayName("chaque méthode de façade délègue au use case correspondant")
    void chaqueMethodeDelegue() {
        UUID id = UUID.randomUUID();
        CandidatureDetail detail = new CandidatureDetail(id, null, "M", "N", "e", "t", null, null, null, true, null);
        CandidaturePage page = new CandidaturePage(java.util.List.of(), 0, 20, 0, 0);

        when(soumettreCandidatureUseCase.soumettre(any())).thenReturn(detail);
        when(getCandidatureUseCase.obtenir(any())).thenReturn(detail);
        when(listCandidaturesUseCase.lister(any())).thenReturn(page);

        assertThat(sut.soumettreCandidature(new SoumettreCandidatureCommand(id, "M", "N", "e", "t", null, null,
                null, true))).isSameAs(detail);
        assertThat(sut.obtenirCandidature(new GetCandidatureQuery(id))).isSameAs(detail);
        assertThat(sut.listerCandidatures(new ListCandidaturesQuery(null, null, null, null, null, null)))
                .isSameAs(page);
    }
}
