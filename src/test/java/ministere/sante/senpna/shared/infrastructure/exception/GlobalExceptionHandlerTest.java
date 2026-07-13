package ministere.sante.senpna.shared.infrastructure.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;
import ministere.sante.senpna.shared.domain.exception.ConflictException;
import ministere.sante.senpna.shared.domain.exception.ForbiddenException;
import ministere.sante.senpna.shared.domain.exception.NotFoundException;
import ministere.sante.senpna.shared.domain.exception.UnauthorizedException;
import ministere.sante.senpna.shared.domain.exception.ValidationException;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler — mapping des exceptions transversales infrastructure")
class GlobalExceptionHandlerTest {

    GlobalExceptionHandler sut = new GlobalExceptionHandler();

    @Nested
    @DisplayName("exceptions métier génériques (shared)")
    class ExceptionsGeneriques {

        @Test
        @DisplayName("UnauthorizedException → 401")
        void unauthorized_401() {
            var response = sut.handleUnauthorized(new UnauthorizedException("msg", "TYPE"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("ForbiddenException → 403")
        void forbidden_403() {
            var response = sut.handleForbidden(new ForbiddenException("msg", "TYPE"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("NotFoundException → 404")
        void notFound_404() {
            var response = sut.handleNotFound(new NotFoundException("msg", "TYPE"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("BusinessRuleException → 422")
        void businessRule_422() {
            var response = sut.handleBusinessRule(new BusinessRuleException("msg", "TYPE"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        @Test
        @DisplayName("ConflictException → 409")
        void conflict_409() {
            var response = sut.handleConflict(new ConflictException("msg", "TYPE"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("ValidationException → 400")
        void validation_400() {
            var response = sut.handleValidation(new ValidationException("msg", "TYPE"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("exceptions Spring MVC / Bean Validation")
    class ExceptionsSpringMvc {

        @Test
        @DisplayName("MethodArgumentNotValidException → 400 avec erreurs de champ")
        void methodArgumentNotValid_400() throws NoSuchMethodException {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "objet");
            bindingResult.addError(new org.springframework.validation.FieldError("objet", "champ", "invalide"));
            var ex = new MethodArgumentNotValidException(
                    new org.springframework.core.MethodParameter(
                            this.getClass().getDeclaredMethod("methodArgumentNotValid_400"), -1),
                    bindingResult);

            var response = sut.handleMethodArgumentNotValid(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("ConstraintViolationException → 400, agrège les messages de violation")
        void constraintViolation_400() {
            var response = sut.handleConstraintViolation(new ConstraintViolationException(Set.of()));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("message", "Paramètre invalide");
        }

        @Test
        @DisplayName("NoResourceFoundException → 404")
        void noResourceFound_404() {
            var response = sut.handleNoResourceFound(
                    new NoResourceFoundException(HttpMethod.GET, "inexistante"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("HttpMessageNotReadableException → 400")
        void messageNotReadable_400() {
            var response = sut.handleMessageNotReadable(
                    new HttpMessageNotReadableException("JSON malformé",
                            (org.springframework.http.HttpInputMessage) null));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("HttpRequestMethodNotSupportedException → 405")
        void methodNotSupported_405() {
            var response = sut.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("POST"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }

        @Test
        @DisplayName("MissingServletRequestParameterException → 400, cite le paramètre manquant")
        void missingParam_400() {
            var response = sut.handleMissingParam(
                    new MissingServletRequestParameterException("page", "Integer"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("message", "Le paramètre 'page' est obligatoire");
        }

        @Test
        @DisplayName("AuthorizationDeniedException → 403")
        void authorizationDenied_403() {
            var response = sut.handleAuthorizationDenied(new AuthorizationDeniedException("refusé"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("fallback")
    class Fallback {

        @Test
        @DisplayName("exception inattendue → 500, message générique (pas de fuite de détails techniques)")
        void exceptionInattendue_500SansFuiteDeDetails() {
            var response = sut.handleUnexpected(new RuntimeException("détail technique sensible"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().get("message").toString())
                    .doesNotContain("détail technique sensible");
        }
    }
}
