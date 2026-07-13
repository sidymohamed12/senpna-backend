package ministere.sante.senpna.config;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.security.AuthUserPrincipal;
import ministere.sante.senpna.config.AppProperties.JwtProperties;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.TokenRevocationPort;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — authentification par Bearer token")
class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256!!";

    @Mock
    UserDetailsService userDetailsService;
    @Mock
    TokenRevocationPort tokenRevocationPort;
    @Mock
    FilterChain filterChain;

    JwtService jwtService;
    Clock clock;
    JwtAuthenticationFilter sut;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        AppProperties appProperties = new AppProperties(
                new JwtProperties(SECRET, Duration.ofMinutes(15), Duration.ofDays(7)),
                null, null, null, null, null);
        jwtService = new JwtService(appProperties, clock);
        sut = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRevocationPort);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AuthUserPrincipal principal() {
        User user = UserFixtures.actif();
        return new AuthUserPrincipal(user, Set.of("GESTIONNAIRE_PNA"));
    }

    @Nested
    @DisplayName("absence ou format d'en-tête Authorization")
    class EnteteAuthorization {

        @Test
        @DisplayName("aucun en-tête Authorization → chaîne appelée, pas d'authentification")
        void sansEntete_chaineAppeleeSansAuth() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("en-tête sans préfixe \"Bearer \" → ignoré")
        void entetesSansPrefixeBearer_ignore() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("révocation du token")
    class Revocation {

        @Test
        @DisplayName("token révoqué → aucune authentification positionnée")
        void tokenRevoque_pasAuthentification() throws Exception {
            String token = jwtService.generateAccessToken(UserFixtures.EMAIL, Map.of());
            when(tokenRevocationPort.estInvalide(token)).thenReturn(true);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }
    }

    @Nested
    @DisplayName("authentification réussie")
    class AuthentificationReussie {

        @Test
        @DisplayName("token valide → SecurityContext peuplé avec le UserDetails résolu")
        void tokenValide_securityContextPeuple() throws Exception {
            String token = jwtService.generateAccessToken(UserFixtures.EMAIL, Map.of());
            when(tokenRevocationPort.estInvalide(token)).thenReturn(false);
            when(userDetailsService.loadUserByUsername(UserFixtures.EMAIL)).thenReturn(principal());

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(
                    UserFixtures.EMAIL);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("affecte entrepotId/structureSanitaireId du token au principal résolu")
        void affecteScopeOrganisationnel() throws Exception {
            UUID entrepotId = UUID.randomUUID();
            String token = jwtService.generateAccessToken(UserFixtures.EMAIL,
                    Map.of("entrepotId", entrepotId.toString()));
            when(tokenRevocationPort.estInvalide(token)).thenReturn(false);
            AuthUserPrincipal principal = principal();
            when(userDetailsService.loadUserByUsername(UserFixtures.EMAIL)).thenReturn(principal);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            assertThat(principal.getEntrepotId()).isEqualTo(entrepotId);
        }
    }

    @Nested
    @DisplayName("erreurs de traitement du token")
    class ErreursTraitement {

        @Test
        @DisplayName("token malformé → aucune exception propagée, chaîne appelée normalement")
        void tokenMalforme_neLevePasException() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Bearer ceci-nest-pas-un-jwt");
            MockHttpServletResponse response = new MockHttpServletResponse();

            org.assertj.core.api.Assertions.assertThatCode(
                    () -> sut.doFilterInternal(request, response, filterChain))
                    .doesNotThrowAnyException();

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("authentification déjà présente dans le contexte → non écrasée")
        void authentificationDejaPresente_nonEcrasee() throws Exception {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken dejaAuthentifie = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "deja-authentifie", null, java.util.List.of());
            SecurityContextHolder.getContext().setAuthentication(dejaAuthentifie);

            String token = jwtService.generateAccessToken(UserFixtures.EMAIL, Map.of());
            when(tokenRevocationPort.estInvalide(token)).thenReturn(false);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("deja-authentifie");
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }
    }
}
