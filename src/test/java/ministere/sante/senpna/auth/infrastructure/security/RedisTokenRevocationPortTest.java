package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Reprend les scénarios qui vivaient auparavant dans {@code JwtTokenAdapterTest}
 * avant la migration vers jwt-toolkit — la logique SHA-256/Redis/TTL a
 * déménagé dans cet adapter dédié, elle garde sa couverture de tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisTokenRevocationPort")
class RedisTokenRevocationPortTest {

    @Mock
    private CachePort cachePort;

    private RedisTokenRevocationPort sut;

    @BeforeEach
    void setup() {
        sut = new RedisTokenRevocationPort(cachePort);
    }

    @Nested
    @DisplayName("revoke()")
    class Revoke {

        @Test
        @DisplayName("stocke le SHA-256 du token en cache avec la TTL fournie")
        void revoke_stocke_fingerprint_avec_ttl() {
            String token = "header.payload.signature";

            sut.revoke(token, Duration.ofMinutes(5));

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(cachePort).put(keyCaptor.capture(), eq("1"), eq(Duration.ofMinutes(5)));
            assertThat(keyCaptor.getValue()).isEqualTo("auth:revoked:" + sha256(token));
        }

        @Test
        @DisplayName("TTL nulle ou négative — ignore l'insertion (inutile)")
        void revoke_ttl_non_positive_skip() {
            sut.revoke("token", Duration.ZERO);
            sut.revoke("token", Duration.ofSeconds(-5));

            verifyNoInteractions(cachePort);
        }
    }

    @Nested
    @DisplayName("isRevoked()")
    class IsRevoked {

        @Test
        @DisplayName("token présent en cache → true")
        void isRevoked_present_retourne_true() {
            String token = "revoked.token";
            when(cachePort.get("auth:revoked:" + sha256(token))).thenReturn(Optional.of("1"));

            assertThat(sut.isRevoked(token)).isTrue();
        }

        @Test
        @DisplayName("token absent du cache → false")
        void isRevoked_absent_retourne_false() {
            String token = "valid.token";
            when(cachePort.get("auth:revoked:" + sha256(token))).thenReturn(Optional.empty());

            assertThat(sut.isRevoked(token)).isFalse();
        }

        @Test
        @DisplayName("erreur cache (exception) → fail-open : retourne false")
        void isRevoked_erreur_cache_fail_open() {
            when(cachePort.get(anyString())).thenThrow(new RuntimeException("Redis down"));

            assertThat(sut.isRevoked("any.token")).isFalse();
        }
    }

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
