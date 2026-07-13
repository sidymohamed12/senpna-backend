package ministere.sante.senpna.shared.infrastructure.web.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

@DisplayName("RequestLoggingInterceptor — logging des requêtes HTTP avec contexte MDC")
class RequestLoggingInterceptorTest {

    RequestLoggingInterceptor sut;

    @BeforeEach
    void setUp() {
        sut = new RequestLoggingInterceptor();
        MDC.clear();
    }

    @Nested
    @DisplayName("preHandle()")
    class PreHandle {

        @Test
        @DisplayName("handler applicatif sur route métier → MDC alimenté, header X-Request-Id positionné")
        void handlerApplicatif_mdcAlimente() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            MockHttpServletResponse response = new MockHttpServletResponse();
            HandlerMethod handler = handlerMethodMock();

            boolean result = sut.preHandle(request, response, handler);

            assertThat(result).isTrue();
            assertThat(MDC.get("method")).isEqualTo("GET");
            assertThat(MDC.get("path")).isEqualTo("/api/medicaments");
            assertThat(MDC.get("requestId")).isNotBlank();
            assertThat(response.getHeader("X-Request-Id")).isEqualTo(MDC.get("requestId"));
        }

        @Test
        @DisplayName("route technique (/actuator, /swagger-ui...) → ignorée, pas de MDC")
        void routeTechnique_ignoree() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
            MockHttpServletResponse response = new MockHttpServletResponse();
            HandlerMethod handler = handlerMethodMock();

            boolean result = sut.preHandle(request, response, handler);

            assertThat(result).isTrue();
            assertThat(MDC.get("requestId")).isNull();
            assertThat(response.getHeader("X-Request-Id")).isNull();
        }

        @Test
        @DisplayName("handler qui n'est pas un HandlerMethod (ex: ressource statique) → ignoré")
        void handlerNonMethode_ignore() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/static/logo.png");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = sut.preHandle(request, response, new Object());

            assertThat(result).isTrue();
            assertThat(MDC.get("requestId")).isNull();
        }

        @Test
        @DisplayName("chaque appel génère un requestId différent")
        void requestId_uniquePourChaqueAppel() {
            MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/x");
            MockHttpServletResponse response1 = new MockHttpServletResponse();
            sut.preHandle(request1, response1, handlerMethodMock());
            String firstId = MDC.get("requestId");

            MDC.clear();

            MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/y");
            MockHttpServletResponse response2 = new MockHttpServletResponse();
            sut.preHandle(request2, response2, handlerMethodMock());
            String secondId = MDC.get("requestId");

            assertThat(firstId).isNotEqualTo(secondId);
        }
    }

    @Nested
    @DisplayName("afterCompletion()")
    class AfterCompletion {

        @Test
        @DisplayName("nettoie systématiquement le MDC après la requête")
        void nettoieLeMdc() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(200);
            sut.preHandle(request, response, handlerMethodMock());
            assertThat(MDC.get("requestId")).isNotNull();

            sut.afterCompletion(request, response, handlerMethodMock(), null);

            assertThat(MDC.get("requestId")).isNull();
            assertThat(MDC.get("method")).isNull();
            assertThat(MDC.get("path")).isNull();
            assertThat(MDC.get("ip")).isNull();
        }

        @Test
        @DisplayName("route technique → ignorée, aucune interaction avec le MDC")
        void routeTechnique_ignoree() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.afterCompletion(request, response, handlerMethodMock(), null);

            assertThat(MDC.get("requestId")).isNull();
        }

        @Test
        @DisplayName("exception applicative → nettoyage du MDC malgré tout")
        void avecException_mdcNettoyeQuandMeme() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            MockHttpServletResponse response = new MockHttpServletResponse();
            sut.preHandle(request, response, handlerMethodMock());

            sut.afterCompletion(request, response, handlerMethodMock(), new RuntimeException("boom"));

            assertThat(MDC.get("requestId")).isNull();
        }

        @Test
        @DisplayName("aucun start-time enregistré (preHandle jamais appelé) → ne lève pas d'exception")
        void sansStartTime_neLeveRien() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            MockHttpServletResponse response = new MockHttpServletResponse();

            assertThatCode(() -> sut.afterCompletion(request, response, handlerMethodMock(), null))
                    .doesNotThrowAnyException();
        }
    }

    private HandlerMethod handlerMethodMock() {
        return mock(HandlerMethod.class);
    }
}
