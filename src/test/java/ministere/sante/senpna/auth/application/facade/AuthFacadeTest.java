package ministere.sante.senpna.auth.application.facade;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ForgotPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LogoutCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.MeQuery;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResendOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.auth.domain.port.in.ForgotPasswordUseCase;
import ministere.sante.senpna.auth.domain.port.in.LoginUseCase;
import ministere.sante.senpna.auth.domain.port.in.LogoutUseCase;
import ministere.sante.senpna.auth.domain.port.in.MeUseCase;
import ministere.sante.senpna.auth.domain.port.in.RefreshTokenUseCase;
import ministere.sante.senpna.auth.domain.port.in.ResendOtpUseCase;
import ministere.sante.senpna.auth.domain.port.in.ResetPasswordUseCase;
import ministere.sante.senpna.auth.domain.port.in.VerifyOtpUseCase;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFacade — délégation aux use cases")
class AuthFacadeTest {

    @Mock
    LoginUseCase loginUseCase;
    @Mock
    ForgotPasswordUseCase forgotPasswordUseCase;
    @Mock
    ResendOtpUseCase resendOtpUseCase;
    @Mock
    VerifyOtpUseCase verifyOtpUseCase;
    @Mock
    ResetPasswordUseCase resetPasswordUseCase;
    @Mock
    RefreshTokenUseCase refreshTokenUseCase;
    @Mock
    MeUseCase meUseCase;
    @Mock
    LogoutUseCase logoutUseCase;

    AuthFacade sut;

    @BeforeEach
    void setUp() {
        sut = new AuthFacade(loginUseCase, forgotPasswordUseCase, resendOtpUseCase, verifyOtpUseCase,
                resetPasswordUseCase, refreshTokenUseCase, meUseCase, logoutUseCase);
    }

    @Test
    @DisplayName("chaque méthode de façade délègue au use case correspondant")
    void chaqueMethodeDelegue() {
        LoginResult loginResult = new LoginResult(new AuthTokens("a", "r", 900L),
                new UserSummary(null, "N", "P", "e", Set.of(), null, null));
        when(loginUseCase.login(any())).thenReturn(loginResult);
        VerifyOtpResult verifyResult = new VerifyOtpResult("reset-token");
        when(verifyOtpUseCase.verifier(any())).thenReturn(verifyResult);
        AuthTokens refreshed = new AuthTokens("a2", "r2", 900L);
        when(refreshTokenUseCase.rafraichir(any())).thenReturn(refreshed);
        UserSummary me = new UserSummary(null, "N", "P", "e", Set.of(), null, null);
        when(meUseCase.me(any())).thenReturn(me);

        assertThat(sut.login(new LoginCommand("e", "p"))).isSameAs(loginResult);
        sut.forgotPassword(new ForgotPasswordCommand("e", OtpChannel.EMAIL));
        verify(forgotPasswordUseCase).demander(any());
        sut.resendOtp(new ResendOtpCommand("e", OtpChannel.EMAIL));
        verify(resendOtpUseCase).renvoyer(any());
        assertThat(sut.verifyOtp(new VerifyOtpCommand("e", "123456"))).isSameAs(verifyResult);
        sut.resetPassword(new ResetPasswordCommand("t", "p"));
        verify(resetPasswordUseCase).reinitialiser(any());
        assertThat(sut.refresh(new RefreshTokenCommand("t"))).isSameAs(refreshed);
        assertThat(sut.me(new MeQuery("e"))).isSameAs(me);
        sut.logout(new LogoutCommand("a", "r"));
        verify(logoutUseCase).logout(any());
    }
}
