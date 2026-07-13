package ministere.sante.senpna.config;

import ministere.sante.senpna.config.AppProperties.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService — génération et validation des jetons JWT")
class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256!!";
    private Clock clock;
    private Instant now;

    JwtService sut;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        clock = Clock.fixed(now, ZoneOffset.UTC);

        AppProperties appProperties = new AppProperties(
                new JwtProperties(
                        SECRET,
                        Duration.ofMinutes(15),
                        Duration.ofDays(7)),
                null, null, null, null, null);

        sut = new JwtService(appProperties, clock);
    }

    private UserDetails userDetails(String username) {
        return new User(username, "hash", java.util.List.of());
    }

    @Nested
    @DisplayName("generateAccessToken() / extractUsername()")
    class GenerateAccessToken {

        @Test
        @DisplayName("génère un token dont le subject correspond au username fourni")
        void subjectCorrespondAuUsername() {
            String token = sut.generateAccessToken("alice@sante.gouv.sn", Map.of());

            assertThat(sut.extractUsername(token)).isEqualTo("alice@sante.gouv.sn");
        }

        @Test
        @DisplayName("les claims additionnels sont bien inclus dans le token")
        void claimsAdditionnelsInclus() {
            UUID entrepotId = UUID.randomUUID();
            String token = sut.generateAccessToken("alice@sante.gouv.sn",
                    Map.of("entrepotId", entrepotId.toString()));

            assertThat(sut.extractEntrepotId(token)).isEqualTo(entrepotId);
        }

        @Test
        @DisplayName("la date d'expiration est dans le futur, proche du TTL configuré")
        void expirationDansLeFutur() {
            String token = sut.generateAccessToken("alice@sante.gouv.sn", Map.of());

            Date expiration = sut.extractExpiration(token);

            assertThat(expiration)
                    .isBetween(
                            Date.from(now.plus(Duration.ofMinutes(15)).minusSeconds(1)),
                            Date.from(now.plus(Duration.ofMinutes(15)).plusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("generateRefreshToken() / isRefreshToken()")
    class GenerateRefreshToken {

        @Test
        @DisplayName("un refresh token est identifié comme tel")
        void refreshTokenIdentifie() {
            String token = sut.generateRefreshToken("alice@sante.gouv.sn");

            assertThat(sut.isRefreshToken(token)).isTrue();
        }

        @Test
        @DisplayName("un access token n'est pas identifié comme un refresh token")
        void accessTokenNestPasRefresh() {
            String token = sut.generateAccessToken("alice@sante.gouv.sn", Map.of());

            assertThat(sut.isRefreshToken(token)).isFalse();
        }

        @Test
        @DisplayName("le subject du refresh token correspond au username")
        void subjectDuRefreshToken() {
            String token = sut.generateRefreshToken("alice@sante.gouv.sn");

            assertThat(sut.extractUsername(token)).isEqualTo("alice@sante.gouv.sn");
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("token valide pour le UserDetails correspondant → true")
        void tokenValidePourUtilisateurCorrespondant() {
            String token = sut.generateAccessToken("alice@sante.gouv.sn", Map.of());

            assertThat(sut.isTokenValid(token, userDetails("alice@sante.gouv.sn"))).isTrue();
        }

        @Test
        @DisplayName("token valide mais username différent → false")
        void tokenValideMaisUsernameDifferent() {
            String token = sut.generateAccessToken("alice@sante.gouv.sn", Map.of());

            assertThat(sut.isTokenValid(token, userDetails("bob@sante.gouv.sn"))).isFalse();
        }

        @Test
        @DisplayName("token malformé → false, ne lève pas d'exception")
        void tokenMalforme_false() {
            assertThat(sut.isTokenValid("ceci-nest-pas-un-jwt", userDetails("alice@sante.gouv.sn"))).isFalse();
        }

        @Test
        @DisplayName("token signé avec une autre clé → false")
        void tokenSigneAvecAutreCle_false() {
            AppProperties autreProperties = new AppProperties(
                    new JwtProperties("une-toute-autre-cle-secrete-de-256-bits-minimum-pour-hmac!!",
                            Duration.ofMinutes(15), Duration.ofDays(7)),
                    null, null, null, null, null);
            JwtService autreService = new JwtService(autreProperties, clock);
            String tokenSigneAilleurs = autreService.generateAccessToken("alice@sante.gouv.sn", Map.of());

            assertThat(sut.isTokenValid(tokenSigneAilleurs, userDetails("alice@sante.gouv.sn"))).isFalse();
        }
    }

    @Nested
    @DisplayName("extraction de claims métier")
    class ExtractionClaims {

        @Test
        @DisplayName("extractEntrepotId() renvoie null si le claim est absent")
        void entrepotIdAbsent_null() {
            String token = sut.generateAccessToken("alice@sante.gouv.sn", Map.of());

            assertThat(sut.extractEntrepotId(token)).isNull();
        }

        @Test
        @DisplayName("extractStructureSanitaireId() résout correctement le claim présent")
        void structureSanitaireIdPresent() {
            UUID structureId = UUID.randomUUID();
            String token = sut.generateAccessToken("alice@sante.gouv.sn",
                    Map.of("structureSanitaireId", structureId.toString()));

            assertThat(sut.extractStructureSanitaireId(token)).isEqualTo(structureId);
        }

        @Test
        @DisplayName("extractClaim() sur un token expiré lève ExpiredJwtException")
        void tokenExpire_leveException() {

            AppProperties properties = new AppProperties(
                    new JwtProperties(
                            SECRET,
                            Duration.ofMinutes(15),
                            Duration.ofDays(7)),
                    null, null, null, null, null);

            Instant creation = Instant.parse("2026-07-13T10:00:00Z");

            JwtService generationService = new JwtService(
                    properties,
                    Clock.fixed(creation, ZoneOffset.UTC));

            String token = generationService.generateAccessToken(
                    "alice@sante.gouv.sn",
                    Map.of());

            JwtService validationService = new JwtService(
                    properties,
                    Clock.offset(
                            Clock.fixed(creation, ZoneOffset.UTC),
                            Duration.ofMinutes(16)));

            assertThatThrownBy(() -> validationService.extractUsername(token))
                    .isInstanceOf(ExpiredJwtException.class);
        }
    }
}
