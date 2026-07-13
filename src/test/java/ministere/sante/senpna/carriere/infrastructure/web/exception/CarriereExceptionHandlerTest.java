package ministere.sante.senpna.carriere.infrastructure.web.exception;

import ministere.sante.senpna.carriere.domain.exception.CandidatureIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.CiviliteInvalideException;
import ministere.sante.senpna.carriere.domain.exception.ConsentementRgpdRequisException;
import ministere.sante.senpna.carriere.domain.exception.DateLimiteCandidatureInvalideException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteFermeeException;
import ministere.sante.senpna.carriere.domain.exception.StatutOpportuniteInvalideException;
import ministere.sante.senpna.carriere.domain.exception.TypeContratInvalideException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CarriereExceptionHandler — mapping des exceptions du module carriere")
class CarriereExceptionHandlerTest {

    CarriereExceptionHandler sut = new CarriereExceptionHandler();

    @Test
    @DisplayName("OpportuniteCarriereIntrouvableException → 404")
    void opportuniteIntrouvable_404() {
        assertThat(sut.handleOpportuniteIntrouvable(new OpportuniteCarriereIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("CandidatureIntrouvableException → 404")
    void candidatureIntrouvable_404() {
        assertThat(sut.handleCandidatureIntrouvable(new CandidatureIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("TypeContratInvalideException → 400")
    void typeContratInvalide_400() {
        assertThat(sut.handleTypeContratInvalide(new TypeContratInvalideException("X")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("StatutOpportuniteInvalideException → 400")
    void statutInvalide_400() {
        assertThat(sut.handleStatutInvalide(new StatutOpportuniteInvalideException("X")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("CiviliteInvalideException → 400")
    void civiliteInvalide_400() {
        assertThat(sut.handleCiviliteInvalide(new CiviliteInvalideException("X")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("ConsentementRgpdRequisException → 400")
    void consentementRgpdRequis_400() {
        assertThat(sut.handleConsentementRgpdRequis(new ConsentementRgpdRequisException()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("DateLimiteCandidatureInvalideException → 400")
    void dateLimiteInvalide_400() {
        assertThat(sut.handleDateLimiteInvalide(new DateLimiteCandidatureInvalideException("X")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("OpportuniteFermeeException → 422")
    void opportuniteFermee_422() {
        assertThat(sut.handleOpportuniteFermee(new OpportuniteFermeeException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
