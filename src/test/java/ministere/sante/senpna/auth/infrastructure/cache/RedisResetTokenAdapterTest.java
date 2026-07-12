package ministere.sante.senpna.auth.infrastructure.cache;

import ministere.sante.senpna.auth.domain.exception.ResetTokenInvalideException;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisResetTokenAdapter")
class RedisResetTokenAdapterTest {

    @Mock
    private CachePort cachePort;
    @InjectMocks
    private RedisResetTokenAdapter sut;

    private static final UUID USER_ID = UserFixtures.USER_ID;
    private static final String EMAIL = UserFixtures.EMAIL;

    // ══════════════════════════════════════════════════════════════════════
    // genererResetToken
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("genererResetToken()")
    class GenererResetToken {

        @Test
        @DisplayName("stocke userId|email avec préfixe 'auth:reset:' et TTL de 5 min")
        void generer_stocke_avec_bon_format() {
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

            String token = sut.genererResetToken(USER_ID, EMAIL);

            verify(cachePort).put(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture());

            assertThat(keyCaptor.getValue()).startsWith("auth:reset:");
            assertThat(keyCaptor.getValue()).contains(token);
            assertThat(valueCaptor.getValue()).isEqualTo(USER_ID + "|" + EMAIL);
            assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("retourne un token UUID non null")
        void generer_retourne_token_non_null() {
            String token = sut.genererResetToken(USER_ID, EMAIL);

            assertThat(token).isNotBlank();
            // Vérifie que c'est un UUID valide
            assertThatNoException().isThrownBy(() -> UUID.fromString(token));
        }

        @Test
        @DisplayName("chaque appel génère un token distinct")
        void generer_tokens_uniques() {
            String token1 = sut.genererResetToken(USER_ID, EMAIL);
            String token2 = sut.genererResetToken(USER_ID, EMAIL);

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // validerEtExtraireUserId
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validerEtExtraireUserId()")
    class ValiderEtExtraire {

        @Test
        @DisplayName("token valide → retourne le userId et consomme le token (evict)")
        void valider_succes_retourne_userId_et_evince() {
            String resetToken = UUID.randomUUID().toString();
            when(cachePort.get("auth:reset:" + resetToken))
                    .thenReturn(Optional.of(USER_ID + "|" + EMAIL));

            UUID result = sut.validerEtExtraireUserId(resetToken);

            assertThat(result).isEqualTo(USER_ID);
            verify(cachePort).evict("auth:reset:" + resetToken); // usage unique
        }

        @Test
        @DisplayName("token absent (expiré ou invalide) → ResetTokenInvalideException")
        void valider_token_absent_leve_exception() {
            String resetToken = "bad-token";
            when(cachePort.get("auth:reset:" + resetToken)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.validerEtExtraireUserId(resetToken))
                    .isInstanceOf(ResetTokenInvalideException.class);

            verify(cachePort, never()).evict(anyString());
        }

        @Test
        @DisplayName("valeur corrompue en Redis (format invalide) → ResetTokenInvalideException")
        void valider_valeur_corrompue_leve_exception() {
            String resetToken = UUID.randomUUID().toString();
            when(cachePort.get("auth:reset:" + resetToken))
                    .thenReturn(Optional.of("VALEUR_SANS_SEPARATEUR"));

            assertThatThrownBy(() -> sut.validerEtExtraireUserId(resetToken))
                    .isInstanceOf(ResetTokenInvalideException.class);
        }

        @Test
        @DisplayName("le token est consommé avant le parsing (fail-fast si le parsing échoue)")
        void valider_evict_avant_parsing() {
            // L'evict doit être appelé même si le parsing du userId échoue
            String resetToken = UUID.randomUUID().toString();
            when(cachePort.get("auth:reset:" + resetToken))
                    .thenReturn(Optional.of("not-a-uuid|email@test.sn"));

            assertThatThrownBy(() -> sut.validerEtExtraireUserId(resetToken))
                    .isInstanceOf(Exception.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // invaliderResetToken
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("invaliderResetToken()")
    class InvaliderResetToken {

        @Test
        @DisplayName("appelle cachePort.evict avec la clé correcte")
        void invalider_appelle_evict() {
            String resetToken = UUID.randomUUID().toString();

            sut.invaliderResetToken(resetToken);

            verify(cachePort).evict("auth:reset:" + resetToken);
        }

        @Test
        @DisplayName("silencieux si le token est déjà absent (idempotent)")
        void invalider_silencieux_si_absent() {
            assertThatNoException()
                    .isThrownBy(() -> sut.invaliderResetToken("token-qui-nexiste-pas"));
        }
    }
}
