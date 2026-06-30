package ministere.sante.senpna.auth.application.facade;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ForgotPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;
import ministere.sante.senpna.auth.domain.command.AuthCommands.MeQuery;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResendOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.auth.domain.port.in.ForgotPasswordUseCase;
import ministere.sante.senpna.auth.domain.port.in.LoginUseCase;
import ministere.sante.senpna.auth.domain.port.in.MeUseCase;
import ministere.sante.senpna.auth.domain.port.in.RefreshTokenUseCase;
import ministere.sante.senpna.auth.domain.port.in.ResendOtpUseCase;
import ministere.sante.senpna.auth.domain.port.in.ResetPasswordUseCase;
import ministere.sante.senpna.auth.domain.port.in.VerifyOtpUseCase;

import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    private final LoginUseCase loginUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResendOtpUseCase resendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final MeUseCase meUseCase;

    public AuthFacade(
            LoginUseCase loginUseCase,
            ForgotPasswordUseCase forgotPasswordUseCase,
            ResendOtpUseCase resendOtpUseCase,
            VerifyOtpUseCase verifyOtpUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            MeUseCase meUseCase) {
        this.loginUseCase = loginUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resendOtpUseCase = resendOtpUseCase;
        this.verifyOtpUseCase = verifyOtpUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.meUseCase = meUseCase;
    }

    public LoginResult login(LoginCommand command) {
        return loginUseCase.login(command);
    }

    public void forgotPassword(ForgotPasswordCommand command) {
        forgotPasswordUseCase.demander(command);
    }

    public void resendOtp(ResendOtpCommand command) {
        resendOtpUseCase.renvoyer(command);
    }

    public VerifyOtpResult verifyOtp(VerifyOtpCommand command) {
        return verifyOtpUseCase.verifier(command);
    }

    public void resetPassword(ResetPasswordCommand command) {
        resetPasswordUseCase.reinitialiser(command);
    }

    public AuthTokens refresh(RefreshTokenCommand command) {
        return refreshTokenUseCase.rafraichir(command);
    }

    public UserSummary me(MeQuery query) {
        return meUseCase.me(query);
    }
}
