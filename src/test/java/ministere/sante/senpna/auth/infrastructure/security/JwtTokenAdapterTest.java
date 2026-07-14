package ministere.sante.senpna.auth.infrastructure.security;

import io.jsonwebtoken.JwtException;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.config.JwtService;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenAdapter")
class JwtTokenAdapterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private CachePort cachePort;

    @InjectMocks
    private JwtTokenAdapter sut;

    private User user;

    @BeforeEach
    void setup() {
        user = UserFixtures.actif();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Génération
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("genererAccess()")
    class GenererAccess {

        @Test
        @DisplayName("délègue à JwtService avec l'email et le claim 'roles', sans affectation")
        void genererAccess_sans_affectation_delegue_jwt_service() {
            Set<String> roleCodes = Set.of("GESTIONNAIRE_PNA", "PHARMACIEN_PRA");
            when(jwtService.generateAccessToken(anyString(), any())).thenReturn("access.jwt");

            String token = sut.genererAccess(user, roleCodes, null, null, null);

            assertThat(token).isEqualTo("access.jwt");
            verify(jwtService).generateAccessToken(
                    eq(UserFixtures.EMAIL),
                    argThat(claims -> claims.containsKey("roles")
                            && !claims.containsKey("entrepotId")
                            && !claims.containsKey("structureSanitaireId")
                            && !claims.containsKey("fournisseurId")));
        }

        @Test
        @DisplayName("inclut entrepotId et structureSanitaireId dans les claims quand fournis")
        void genererAccess_avec_affectation_inclut_les_claims() {
            Set<String> roleCodes = Set.of("GESTIONNAIRE_PNA");
            UUID entrepotId = UUID.randomUUID();
            UUID structureSanitaireId = UUID.randomUUID();
            when(jwtService.generateAccessToken(anyString(), any())).thenReturn("access.jwt");

            String token = sut.genererAccess(user, roleCodes, entrepotId, structureSanitaireId, null);

            assertThat(token).isEqualTo("access.jwt");
            verify(jwtService).generateAccessToken(
                    eq(UserFixtures.EMAIL),
                    argThat(claims -> entrepotId.toString().equals(claims.get("entrepotId"))
                            && structureSanitaireId.toString().equals(claims.get("structureSanitaireId"))
                            && !claims.containsKey("fournisseurId")));
        }

        @Test
        @DisplayName("entrepotId fourni seul → structureSanitaireId et fournisseurId absents des claims")
        void genererAccess_avec_entrepot_seul() {
            Set<String> roleCodes = Set.of("GESTIONNAIRE_PNA");
            UUID entrepotId = UUID.randomUUID();
            when(jwtService.generateAccessToken(anyString(), any())).thenReturn("access.jwt");

            sut.genererAccess(user, roleCodes, entrepotId, null, null);

            verify(jwtService).generateAccessToken(
                    eq(UserFixtures.EMAIL),
                    argThat(claims -> entrepotId.toString().equals(claims.get("entrepotId"))
                            && !claims.containsKey("structureSanitaireId")
                            && !claims.containsKey("fournisseurId")));
        }

        @Test
        @DisplayName("fournisseurId fourni seul → inclus dans les claims, entrepotId/structureSanitaireId absents")
        void genererAccess_avec_fournisseur_seul() {
            Set<String> roleCodes = Set.of("FOURNISSEUR");
            UUID fournisseurId = UUID.randomUUID();
            when(jwtService.generateAccessToken(anyString(), any())).thenReturn("access.jwt");

            sut.genererAccess(user, roleCodes, null, null, fournisseurId);

            verify(jwtService).generateAccessToken(
                    eq(UserFixtures.EMAIL),
                    argThat(claims -> fournisseurId.toString().equals(claims.get("fournisseurId"))
                            && !claims.containsKey("entrepotId")
                            && !claims.containsKey("structureSanitaireId")));
        }
    }

    @Nested
    @DisplayName("genererRefresh()")
    class GenererRefresh {

        @Test
        @DisplayName("délègue à JwtService avec l'email de l'utilisateur")
        void genererRefresh_delegue_jwt_service() {
            when(jwtService.generateRefreshToken(UserFixtures.EMAIL)).thenReturn("refresh.jwt");

            String token = sut.genererRefresh(user);

            assertThat(token).isEqualTo("refresh.jwt");
            verify(jwtService).generateRefreshToken(UserFixtures.EMAIL);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Révocation
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("invalider()")
    class Invalider {

        @Test
        @DisplayName("stocke le SHA-256 du token en Redis avec la TTL résiduelle")
        void invalider_stocke_fingerprint_avec_ttl_residuelle() {
            String token = "header.payload.signature";
            Date expiration = Date.from(Instant.now().plusSeconds(300));
            when(jwtService.extractExpiration(token)).thenReturn(expiration);

            sut.invalider(token);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(cachePort).put(keyCaptor.capture(), eq("1"), ttlCaptor.capture());

            String expectedKey = "auth:revoked:" + sha256(token);
            assertThat(keyCaptor.getValue()).isEqualTo(expectedKey);
            // TTL doit être positive et proche de 300s
            assertThat(ttlCaptor.getValue().toSeconds()).isBetween(290L, 300L);
        }

        @Test
        @DisplayName("token déjà expiré — ignore l'insertion Redis (inutile)")
        void invalider_token_expire_skip() {
            String token = "expired.token";
            when(jwtService.extractExpiration(token))
                    .thenReturn(Date.from(Instant.now().minusSeconds(10)));

            sut.invalider(token);

            verifyNoInteractions(cachePort);
        }

        @Test
        @DisplayName("token malformé (JwtException) — ignore sans lever d'exception")
        void invalider_token_malformed_silencieux() {
            String token = "not.a.jwt";
            when(jwtService.extractExpiration(token)).thenThrow(new JwtException("malformé"));

            assertThatNoException().isThrownBy(() -> sut.invalider(token));
            verifyNoInteractions(cachePort);
        }
    }

    @Nested
    @DisplayName("estInvalide()")
    class EstInvalide {

        @Test
        @DisplayName("token présent en cache (révoqué) → true")
        void estInvalide_revoque_retourne_true() {
            String token = "revoked.token";
            when(cachePort.get("auth:revoked:" + sha256(token)))
                    .thenReturn(Optional.of("1"));

            assertThat(sut.estInvalide(token)).isTrue();
        }

        @Test
        @DisplayName("token absent du cache → false")
        void estInvalide_non_revoque_retourne_false() {
            String token = "valid.token";
            when(cachePort.get("auth:revoked:" + sha256(token)))
                    .thenReturn(Optional.empty());

            assertThat(sut.estInvalide(token)).isFalse();
        }

        @Test
        @DisplayName("erreur Redis (exception) → fail-open : retourne false")
        void estInvalide_erreur_redis_fail_open() {
            String token = "any.token";
            when(cachePort.get(anyString())).thenThrow(new RuntimeException("Redis down"));

            assertThat(sut.estInvalide(token)).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Extraction
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Extraction de claims")
    class Extraction {

        @Test
        @DisplayName("extraireEmail() délègue à jwtService.extractUsername()")
        void extraireEmail_delegue() {
            when(jwtService.extractUsername("some.jwt")).thenReturn("user@example.com");
            assertThat(sut.extraireEmail("some.jwt")).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("estRefreshToken() délègue à jwtService.isRefreshToken()")
        void estRefreshToken_delegue() {
            when(jwtService.isRefreshToken("refresh.jwt")).thenReturn(true);
            when(jwtService.isRefreshToken("access.jwt")).thenReturn(false);

            assertThat(sut.estRefreshToken("refresh.jwt")).isTrue();
            assertThat(sut.estRefreshToken("access.jwt")).isFalse();
        }

        @Test
        @DisplayName("estExpire() → false si expiration dans le futur")
        void estExpire_futur_retourne_false() {
            when(jwtService.extractExpiration("token"))
                    .thenReturn(Date.from(Instant.now().plusSeconds(300)));

            assertThat(sut.estExpire("token")).isFalse();
        }

        @Test
        @DisplayName("estExpire() → true si expiration dans le passé")
        void estExpire_passe_retourne_true() {
            when(jwtService.extractExpiration("token"))
                    .thenReturn(Date.from(Instant.now().minusSeconds(1)));

            assertThat(sut.estExpire("token")).isTrue();
        }

        @Test
        @DisplayName("estExpire() → true si JwtException (token malformé ou invalide)")
        void estExpire_jwt_exception_retourne_true() {
            when(jwtService.extractExpiration("malformed"))
                    .thenThrow(new JwtException("invalid"));

            assertThat(sut.estExpire("malformed")).isTrue();
        }
    }

    // ── Helper identique à l'implémentation ─────────────────────────────

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
