package ministere.sante.senpna.appeloffre.infrastructure.scheduler;

import ministere.sante.senpna.appeloffre.domain.port.in.ClorerAppelOffresExpiresUseCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppelOffreClotureScheduler — job planifié de clôture automatique")
class AppelOffreClotureSchedulerTest {

    @Mock
    ClorerAppelOffresExpiresUseCase clorerAppelOffresExpiresUseCase;

    @Test
    @DisplayName("délègue au use case de clôture des appels d'offres expirés")
    void delegueAuUseCase() {
        when(clorerAppelOffresExpiresUseCase.clorerExpires()).thenReturn(2);

        new AppelOffreClotureScheduler(clorerAppelOffresExpiresUseCase).clorerAppelOffresExpires();

        verify(clorerAppelOffresExpiresUseCase).clorerExpires();
    }

    @Test
    @DisplayName("exception levée par le use case → interceptée, ne remonte jamais (job planifié)")
    void exceptionInterceptee_neRemonteJamais() {
        when(clorerAppelOffresExpiresUseCase.clorerExpires()).thenThrow(new RuntimeException("erreur base"));

        assertThatCode(() -> new AppelOffreClotureScheduler(clorerAppelOffresExpiresUseCase).clorerAppelOffresExpires())
                .doesNotThrowAnyException();
    }
}
