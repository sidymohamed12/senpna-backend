package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.exception.LotHorsPorteeException;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LotOwnershipGuard")
class LotOwnershipGuardTest {

    @Mock
    private StockRepositoryPort stockRepositoryPort;

    @Mock
    private EntrepotScopeGuard entrepotScopeGuard;

    private LotOwnershipGuard guard;

    @Nested
    @DisplayName("verifierAccesLot()")
    class VerifierAccesLot {

        @Test
        @DisplayName("acteur national (PNA) → toujours autorisé, aucune vérification de stock")
        void acteurNational_toujoursAutorise() {
            guard = new LotOwnershipGuard(stockRepositoryPort, entrepotScopeGuard);
            when(entrepotScopeGuard.estActeurNational()).thenReturn(true);

            guard.verifierAccesLot(LotId.generate()); // ne doit pas lever

            Mockito.verifyNoInteractions(stockRepositoryPort);
        }

        @Test
        @DisplayName("acteur régional (PRA) avec une ligne de stock dans son entrepôt → autorisé")
        void acteurRegionalAvecStock_autorise() {
            guard = new LotOwnershipGuard(stockRepositoryPort, entrepotScopeGuard);
            UUID entrepotId = UUID.randomUUID();
            LotId lotId = LotId.generate();

            when(entrepotScopeGuard.estActeurNational()).thenReturn(false);
            when(entrepotScopeGuard.entrepotIdCourant()).thenReturn(entrepotId);
            when(stockRepositoryPort.findByEntrepotIdAndLotId(EntrepotId.of(entrepotId), lotId))
                    .thenReturn(Optional.of(Mockito.mock(Stock.class)));

            guard.verifierAccesLot(lotId); // ne doit pas lever
        }

        @Test
        @DisplayName("acteur régional (PRA) sans ligne de stock dans son entrepôt → LotHorsPorteeException")
        void acteurRegionalSansStock_refuse() {
            guard = new LotOwnershipGuard(stockRepositoryPort, entrepotScopeGuard);
            UUID entrepotId = UUID.randomUUID();
            LotId lotId = LotId.generate();

            when(entrepotScopeGuard.estActeurNational()).thenReturn(false);
            when(entrepotScopeGuard.entrepotIdCourant()).thenReturn(entrepotId);
            when(stockRepositoryPort.findByEntrepotIdAndLotId(EntrepotId.of(entrepotId), lotId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> guard.verifierAccesLot(lotId)).isInstanceOf(LotHorsPorteeException.class);
        }
    }
}
