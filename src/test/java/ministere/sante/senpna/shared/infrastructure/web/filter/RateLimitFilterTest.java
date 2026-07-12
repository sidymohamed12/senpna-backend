package ministere.sante.senpna.shared.infrastructure.web.filter;

import jakarta.servlet.FilterChain;

import ministere.sante.senpna.shared.domain.port.out.RateLimitPort;
import ministere.sante.senpna.shared.domain.port.out.RateLimitPort.RateLimitResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter — rate limiting par groupe de routes")
class RateLimitFilterTest {

    @Mock
    RateLimitPort rateLimitPort;
    @Mock
    FilterChain filterChain;

    RateLimitFilter sut;

    @BeforeEach
    void setUp() {
        sut = new RateLimitFilter(rateLimitPort);
    }

    @Nested
    @DisplayName("shouldNotFilter()")
    class ShouldNotFilter {

        @Test
        @DisplayName("requête OPTIONS → non filtrée")
        void options_nonFiltree() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");

            assertThat(sut.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("requête GET → filtrée")
        void get_filtree() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");

            assertThat(sut.shouldNotFilter(request)).isFalse();
        }
    }

    @Nested
    @DisplayName("doFilterInternal() — résolution de groupe")
    class ResolutionDeGroupe {

        @Test
        @DisplayName("route hors /api et /webhooks → pas de rate limiting, chaîne appelée directement")
        void routeHorsPerimetre_ignoree() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(rateLimitPort, never()).tryConsume(anyString(), anyInt(), anyLong());
        }

        @Test
        @DisplayName("route d'authentification connue → groupe \"auth\", limite 10 req/min")
        void routeAuth_groupeAuth() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.allow(9, 60_000L));

            sut.doFilterInternal(request, response, filterChain);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(rateLimitPort).tryConsume(keyCaptor.capture(), eq(10), eq(60_000L));
            assertThat(keyCaptor.getValue()).startsWith("auth:");
        }

        @Test
        @DisplayName("route /webhooks/** → groupe \"webhooks\", limite 60 req/min")
        void routeWebhook_groupeWebhook() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhooks/stripe");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.allow(59, 60_000L));

            sut.doFilterInternal(request, response, filterChain);

            verify(rateLimitPort).tryConsume(anyString(), eq(60), eq(60_000L));
        }

        @Test
        @DisplayName("autre route /api/** → groupe \"api\", limite 120 req/min")
        void routeApiGenerique_groupeApi() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.allow(119, 60_000L));

            sut.doFilterInternal(request, response, filterChain);

            verify(rateLimitPort).tryConsume(anyString(), eq(120), eq(60_000L));
        }
    }

    @Nested
    @DisplayName("doFilterInternal() — extraction de l'IP")
    class ExtractionIp {

        @Test
        @DisplayName("X-Forwarded-For présent → première IP de la liste utilisée comme clé")
        void xForwardedFor_priseEnCompte() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.allow(1, 1000L));

            sut.doFilterInternal(request, response, filterChain);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(rateLimitPort).tryConsume(keyCaptor.capture(), anyInt(), anyLong());
            assertThat(keyCaptor.getValue()).isEqualTo("api:203.0.113.5");
        }

        @Test
        @DisplayName("ni X-Forwarded-For ni X-Real-IP → repli sur remoteAddr")
        void repliSurRemoteAddr() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.setRemoteAddr("192.168.1.42");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.allow(1, 1000L));

            sut.doFilterInternal(request, response, filterChain);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(rateLimitPort).tryConsume(keyCaptor.capture(), anyInt(), anyLong());
            assertThat(keyCaptor.getValue()).isEqualTo("api:192.168.1.42");
        }
    }

    @Nested
    @DisplayName("doFilterInternal() — comportement selon le résultat")
    class ComportementSelonResultat {

        @Test
        @DisplayName("requête autorisée → chaîne appelée, headers de rate-limit positionnés")
        void autorisee_chaineAppelee() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.allow(42, 30_000L));

            sut.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("120");
            assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("42");
            assertThat(response.getHeader("X-RateLimit-Reset")).isNotNull();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("requête refusée → 429, chaîne jamais appelée, headers Retry-After présents")
        void refusee_429SansChaine() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.deny(15_000L));

            sut.doFilterInternal(request, response, filterChain);

            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
            assertThat(response.getHeader("Retry-After")).isEqualTo("15");
            assertThat(response.getContentType()).contains("application/json");
            assertThat(response.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("resetAfterMs très faible → Retry-After au moins 1 seconde")
        void retryAfter_minimumUneSeconde() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            when(rateLimitPort.tryConsume(anyString(), anyInt(), anyLong()))
                    .thenReturn(RateLimitResult.deny(200L));

            sut.doFilterInternal(request, response, filterChain);

            assertThat(response.getHeader("Retry-After")).isEqualTo("1");
        }
    }
}
