package ministere.sante.senpna.auth.infrastructure.web.exception;

import ministere.sante.senpna.auth.domain.exception.CompteInactifException;
import ministere.sante.senpna.auth.domain.exception.CompteVerrouilleException;
import ministere.sante.senpna.auth.domain.exception.InvalidCredentialsException;
import ministere.sante.senpna.auth.domain.exception.InvalidRefreshTokenException;
import ministere.sante.senpna.auth.domain.exception.OtpCooldownException;
import ministere.sante.senpna.auth.domain.exception.OtpEnvoiEchoueException;
import ministere.sante.senpna.auth.domain.exception.OtpExpireException;
import ministere.sante.senpna.auth.domain.exception.OtpInvalideException;
import ministere.sante.senpna.auth.domain.exception.OtpTentativesEpuiseesException;
import ministere.sante.senpna.auth.domain.exception.ResetTokenInvalideException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthExceptionHandler — mapping des exceptions du module auth")
class AuthExceptionHandlerTest {

    AuthExceptionHandler sut = new AuthExceptionHandler();

    @Test
    @DisplayName("InvalidCredentialsException → 401")
    void invalidCredentials_401() {
        assertThat(sut.handleInvalidCredentials(new InvalidCredentialsException()).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("OtpInvalideException → 401")
    void otpInvalide_401() {
        assertThat(sut.handleOtpInvalide(new OtpInvalideException()).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("OtpExpireException → 401")
    void otpExpire_401() {
        assertThat(sut.handleOtpExpire(new OtpExpireException()).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("InvalidRefreshTokenException → 401")
    void invalidRefreshToken_401() {
        assertThat(sut.handleInvalidRefreshToken(new InvalidRefreshTokenException()).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("ResetTokenInvalideException → 401")
    void resetTokenInvalide_401() {
        assertThat(sut.handleResetTokenInvalide(new ResetTokenInvalideException()).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("CompteInactifException → 403")
    void compteInactif_403() {
        assertThat(sut.handleCompteInactif(new CompteInactifException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("CompteVerrouilleException → 403")
    void compteVerrouille_403() {
        assertThat(sut.handleCompteVerrouille(new CompteVerrouilleException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("OtpTentativesEpuiseesException → 403")
    void otpTentativesEpuisees_403() {
        assertThat(sut.handleOtpTentativesEpuisees(new OtpTentativesEpuiseesException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("OtpCooldownException → 422")
    void otpCooldown_422() {
        assertThat(sut.handleOtpCooldown(new OtpCooldownException(30)).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("OtpEnvoiEchoueException → 422")
    void otpEnvoiEchoue_422() {
        assertThat(sut.handleOtpEnvoiEchoue(new OtpEnvoiEchoueException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
