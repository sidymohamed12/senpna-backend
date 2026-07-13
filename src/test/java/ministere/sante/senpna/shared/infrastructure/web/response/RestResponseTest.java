package ministere.sante.senpna.shared.infrastructure.web.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RestResponse — enveloppe standard des réponses HTTP")
class RestResponseTest {

    @Nested
    @DisplayName("response()")
    class Response {

        @Test
        @DisplayName("construit le corps avec statut, type, message, timestamp et résultats")
        void construitLeCorps() {
            Map<String, Object> body = RestResponse.response(
                    HttpStatus.OK, List.of("a", "b"), "SUCCESS", "OK");

            assertThat(body)
                    .containsEntry("status", 200)
                    .containsEntry("type", "SUCCESS")
                    .containsEntry("message", "OK")
                    .containsEntry("results", List.of("a", "b"))
                    .containsKey("timestamp");

            assertThat(body.get("timestamp"))
                    .isInstanceOf(String.class)
                    .asString()
                    .isNotBlank();
            assertThat(body).doesNotContainKey("pagination")
                    .doesNotContainKey("errors");
        }

        @Test
        @DisplayName("résultats null → clé \"results\" présente avec valeur null")
        void resultatsNull() {
            Map<String, Object> body = RestResponse.response(HttpStatus.NO_CONTENT, null, "SUCCESS", "Vide");

            assertThat(body).containsEntry("results", null);
        }
    }

    @Nested
    @DisplayName("responsePaginate()")
    class ResponsePaginate {

        @Test
        @DisplayName("ajoute un bloc pagination correctement rempli")
        void ajoutePagination() {
            Map<String, Object> body = RestResponse.responsePaginate(
                    HttpStatus.OK, List.of("a"), "SUCCESS", "OK",
                    2, 5, 42L, false, false);

            @SuppressWarnings("unchecked")
            Map<String, Object> pagination = (Map<String, Object>) body.get("pagination");

            assertThat(pagination)
                    .containsEntry("currentPage", 2)
                    .containsEntry("totalPages", 5)
                    .containsEntry("totalItems", 42L)
                    .containsEntry("first", false)
                    .containsEntry("last", false);

            assertThat(body)
                    .containsEntry("results", List.of("a"));
        }

        @Test
        @DisplayName("première et dernière page à true quand une seule page")
        void pageUnique() {
            Map<String, Object> body = RestResponse.responsePaginate(
                    HttpStatus.OK, List.of(), "SUCCESS", "OK",
                    1, 1, 0L, true, true);

            @SuppressWarnings("unchecked")
            Map<String, Object> pagination = (Map<String, Object>) body.get("pagination");

            assertThat(pagination).containsEntry("first", true).containsEntry("last", true);
        }
    }

    @Nested
    @DisplayName("error()")
    class Error {

        @Test
        @DisplayName("construit un corps d'erreur avec results à null")
        void construitLeCorpsErreur() {
            Map<String, Object> body = RestResponse.error(HttpStatus.NOT_FOUND, "Introuvable", "NOT_FOUND");

            assertThat(body)
                    .containsEntry("status", 404)
                    .containsEntry("type", "NOT_FOUND")
                    .containsEntry("message", "Introuvable")
                    .containsEntry("results", null)
                    .containsKey("timestamp");
            assertThat(body.get("results")).isNull();
        }
    }

    @Nested
    @DisplayName("validationError() / extractFieldErrors()")
    class ValidationError {

        @Test
        @DisplayName("agrège les erreurs de champ sous forme de map champ → message")
        void agregeErreursDeChamp() {
            BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objet");
            bindingResult.addError(new FieldError("objet", "email", "Email invalide"));
            bindingResult.addError(new FieldError("objet", "nom", "Nom obligatoire"));

            Map<String, Object> body = RestResponse.validationError(bindingResult);

            assertThat(body)
                    .containsEntry("status", 400)
                    .containsEntry("type", "VALIDATION_ERROR");

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) body.get("errors");

            assertThat(errors)
                    .containsEntry("email", "Email invalide")
                    .containsEntry("nom", "Nom obligatoire");
        }

        @Test
        @DisplayName("message de champ null → remplacé par \"Valeur invalide\"")
        void messageNullRemplace() {
            BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objet");
            bindingResult.addError(new FieldError("objet", "champ", null));

            Map<String, String> errors = RestResponse.extractFieldErrors(bindingResult);

            assertThat(errors).containsEntry("champ", "Valeur invalide");
        }

        @Test
        @DisplayName("erreurs dupliquées sur le même champ → seule la première est conservée")
        void doublonsSurMemeChamp() {
            BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objet");
            bindingResult.addError(new FieldError("objet", "champ", "Première erreur"));
            bindingResult.addError(new FieldError("objet", "champ", "Deuxième erreur"));

            Map<String, String> errors = RestResponse.extractFieldErrors(bindingResult);

            assertThat(errors).containsEntry("champ", "Première erreur");
        }

        @Test
        @DisplayName("aucune erreur → map vide")
        void aucuneErreur() {
            BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objet");

            assertThat(RestResponse.extractFieldErrors(bindingResult)).isEmpty();
        }
    }

    @Nested
    @DisplayName("classe utilitaire")
    class ClasseUtilitaire {

        @Test
        @DisplayName("constructeur privé et non instanciable")
        void constructeurPriveEtNonInstanciable() throws Exception {
            Constructor<RestResponse> constructor = RestResponse.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();

            constructor.setAccessible(true);

            assertThatThrownBy(() -> invokeConstructor(constructor))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    private static void invokeConstructor(Constructor<RestResponse> constructor) throws Throwable {
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
