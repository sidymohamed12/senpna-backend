package ministere.sante.senpna.stock.infrastructure.scheduler;

import ministere.sante.senpna.stock.domain.port.in.lot.MarquerLotsExpiresUseCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LotExpirationScheduler — job planifié de péremption")
class LotExpirationSchedulerTest {

    @Mock
    MarquerLotsExpiresUseCase marquerLotsExpiresUseCase;

    @Test
    @DisplayName("délègue au use case de marquage des lots expirés")
    void delegueAuUseCase() {
        when(marquerLotsExpiresUseCase.marquerExpires()).thenReturn(3);

        new LotExpirationScheduler(marquerLotsExpiresUseCase).marquerLotsExpires();

        verify(marquerLotsExpiresUseCase).marquerExpires();
    }

    @Test
    @DisplayName("exception levée par le use case → interceptée, ne remonte jamais (job planifié)")
    void exceptionInterceptee_neRemonteJamais() {
        when(marquerLotsExpiresUseCase.marquerExpires()).thenThrow(new RuntimeException("erreur base"));

        assertThatCode(() -> new LotExpirationScheduler(marquerLotsExpiresUseCase).marquerLotsExpires())
                .doesNotThrowAnyException();
    }
}
