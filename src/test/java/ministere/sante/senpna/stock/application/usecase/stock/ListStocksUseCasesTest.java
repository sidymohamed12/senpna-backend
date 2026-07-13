package ministere.sante.senpna.stock.application.usecase.stock;

import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListStocksUseCaseImpl / ListerAlertesRuptureUseCaseImpl")
class ListStocksUseCasesTest {

    @Mock
    StockRepositoryPort stockRepositoryPort;
    @Mock
    StockDetailAssembler stockDetailAssembler;
    @Mock
    EntrepotScopeGuard entrepotScopeGuard;

    @Nested
    @DisplayName("ListStocksUseCaseImpl")
    class ListStocks {

        ListStocksUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ListStocksUseCaseImpl(stockRepositoryPort, stockDetailAssembler, entrepotScopeGuard);
        }

        @Test
        @DisplayName("résout la portée de lecture et délègue à search()")
        void resoutPorteeEtDelegue() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);
            when(stockRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            StockPage page = sut.lister(new ListStocksQuery(null, null, null, null, null, 0, 20, null, null));

            assertThat(page.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ListerAlertesRuptureUseCaseImpl")
    class ListerAlertesRupture {

        ListerAlertesRuptureUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ListerAlertesRuptureUseCaseImpl(stockRepositoryPort, stockDetailAssembler, entrepotScopeGuard);
        }

        @Test
        @DisplayName("seuilAtteintUniquement absent (false) → filtre par rupture (ruptureUniquement=true)")
        void seuilAtteintAbsent_filtreParRupture() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);
            when(stockRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            sut.lister(new ListStocksQuery(null, null, null, null, null, 0, 20, null, null));

            verify(stockRepositoryPort).search(
                    eq(new StockSearchCriteria(null, null, null, Boolean.TRUE, null)), any());
        }

        @Test
        @DisplayName("seuilAtteintUniquement=true → filtre par seuil atteint, pas par rupture")
        void seuilAtteintDemande_filtreParSeuil() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);
            when(stockRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            sut.lister(new ListStocksQuery(null, null, null, null, true, 0, 20, null, null));

            verify(stockRepositoryPort).search(
                    eq(new StockSearchCriteria(null, null, null, null, Boolean.TRUE)), any());
        }
    }
}
