package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereCommandMapper;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.ListOpportunitesCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarrierePage;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListOpportunitesCarriereUseCaseImpl — recherche paginée des opportunités")
class ListOpportunitesCarriereUseCaseImplTest {

    @Mock
    OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    @Mock
    OpportuniteCarriereCommandMapper commandMapper;
    @Mock
    OpportuniteCarriereDetailAssembler assembler;

    ListOpportunitesCarriereUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListOpportunitesCarriereUseCaseImpl(opportuniteCarriereRepositoryPort, commandMapper, assembler);
    }

    @Test
    @DisplayName("aucun résultat → page de contenu vide")
    void aucunResultat_pageVide() {
        when(commandMapper.versTypeContratOptionnel(any())).thenReturn(null);
        when(commandMapper.versStatutOptionnel(any())).thenReturn(null);
        when(opportuniteCarriereRepositoryPort.search(any(), any()))
                .thenReturn(PageResult.of(List.of(), 0, 20, 0));

        OpportuniteCarrierePage page = sut.lister(
                new ListOpportunitesCarriereQuery(null, null, null, false, null, null, null, null));

        assertThat(page.content()).isEmpty();
    }
}
