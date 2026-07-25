package ministere.sante.senpna.config;

import com.sidymohamed12.jwt.core.algorithm.JwtAlgorithm;
import com.sidymohamed12.jwt.core.key.HmacSigningKeyProvider;
import com.sidymohamed12.jwt.core.key.SigningKeyProvider;
import com.sidymohamed12.jwt.core.revocation.TokenRevocationPort;
import com.sidymohamed12.jwt.core.token.DefaultJwtTokenService;
import com.sidymohamed12.jwt.core.token.JwtTokenService;
import com.sidymohamed12.jwt.core.token.JwtTokenSpec;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.security.AuthUserPrincipal;
import ministere.sante.senpna.shared.domain.model.User;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Migré vers jwt-toolkit : le filtre ne dépend plus que de
 * {@link JwtTokenService} + {@link UserDetailsService} (plus de
 * {@code TokenRevocationPort} domaine injecté séparément — la révocation
 * est désormais vérifiée à l'intérieur même de {@code parse()}).
 * <p>
 * On utilise ici une vraie instance de {@link DefaultJwtTokenService}
 * (comme le fait la suite de tests de jwt-core elle-même) plutôt que de
 * mocker la génération de token — plus proche du comportement réel, et on
 * évite de dupliquer la logique de signature dans le test.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — authentification par Bearer token")
class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256!!";

    @Mock
    UserDetailsService userDetailsService;
    @Mock
    TokenRevocationPort revocationPort; // TokenRevocationPort de la librairie (com.sidymohamed12.jwt.core.revocation)
    @Mock
    FilterChain filterChain;

    Clock clock;
    JwtTokenService jwtTokenService;
    JwtAuthenticationFilter sut;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        SigningKeyProvider keyProvider = new HmacSigningKeyProvider(SECRET, JwtAlgorithm.HS256);
        jwtTokenService = new DefaultJwtTokenService(keyProvider, clock, List.of(), revocationPort);
        sut = new JwtAuthenticationFilter(jwtTokenService, userDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String generateToken(String email, Map<String, Object> extraClaims) {
        JwtTokenSpec.Builder builder = JwtTokenSpec.builder().subject(email).ttl(Duration.ofMinutes(15));
        extraClaims.forEach(builder::claim);
        return jwtTokenService.generate(builder.build());
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
            String token = generateToken(UserFixtures.EMAIL, Map.of());
            when(revocationPort.isRevoked(token)).thenReturn(true);

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
            String token = generateToken(UserFixtures.EMAIL, Map.of());
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
            String token = generateToken(UserFixtures.EMAIL, Map.of("entrepotId", entrepotId.toString()));
            AuthUserPrincipal principal = principal();
            when(userDetailsService.loadUserByUsername(UserFixtures.EMAIL)).thenReturn(principal);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            assertThat(principal.getEntrepotId()).isEqualTo(entrepotId);
        }

        @Test
        @DisplayName("affecte fournisseurId du token au principal résolu")
        void affecteFournisseurId() throws Exception {
            UUID fournisseurId = UUID.randomUUID();
            String token = generateToken(UserFixtures.EMAIL, Map.of("fournisseurId", fournisseurId.toString()));
            AuthUserPrincipal principal = principal();
            when(userDetailsService.loadUserByUsername(UserFixtures.EMAIL)).thenReturn(principal);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/medicaments");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            sut.doFilterInternal(request, response, filterChain);

            assertThat(principal.getFournisseurId()).isEqualTo(fournisseurId);
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

            String token = generateToken(UserFixtures.EMAIL, Map.of());

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
