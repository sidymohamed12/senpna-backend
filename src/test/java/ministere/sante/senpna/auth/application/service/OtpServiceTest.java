package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.exception.OtpCooldownException;
import ministere.sante.senpna.auth.domain.exception.OtpExpireException;
import ministere.sante.senpna.auth.domain.exception.OtpInvalideException;
import ministere.sante.senpna.auth.domain.exception.OtpTentativesEpuiseesException;
import ministere.sante.senpna.auth.domain.port.out.OtpSenderPort;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService")
class OtpServiceTest {

    @Mock
    private CachePort cachePort;
    @Mock
    private OtpSenderPort otpSenderPort;
    @Mock
    private AppProperties appProperties;

    private OtpService sut;

    private static final String EMAIL = "mamadou.diallo@sante.gouv.sn";
    private static final String DESTINATION = EMAIL;
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Duration COOLDOWN = Duration.ofMinutes(1);
    private static final int MAX_ATTEMPTS = 3;
    private static final int CODE_LENGTH = 6;

    @BeforeEach
    void setup() {
        AppProperties.OtpProperties otp = mock(AppProperties.OtpProperties.class);
        lenient().when(appProperties.otp()).thenReturn(otp);
        lenient().when(otp.ttl()).thenReturn(TTL);
        lenient().when(otp.cooldown()).thenReturn(COOLDOWN);
        lenient().when(otp.maxAttempts()).thenReturn(MAX_ATTEMPTS);
        lenient().when(otp.length()).thenReturn(CODE_LENGTH);
        sut = new OtpService(cachePort, otpSenderPort, appProperties);
    }

    // ══════════════════════════════════════════════════════════════════════
    // genererEtEnvoyer
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("genererEtEnvoyer()")
    class GenererEtEnvoyer {

        @BeforeEach
        void setup_pas_de_cooldown() {
            when(cachePort.get("otp:cooldown:" + EMAIL)).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("stocke un code à 6 chiffres en cache avec le TTL configuré")
        void genererEtEnvoyer_stocke_code() {
            sut.genererEtEnvoyer(EMAIL, OtpChannel.EMAIL, DESTINATION);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

            verify(cachePort, atLeastOnce()).put(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture());

            // Vérifier le stockage du code
            int codeIndex = keyCaptor.getAllValues().indexOf("otp:code:" + EMAIL);
            assertThat(codeIndex).isNotNegative();
            String code = valueCaptor.getAllValues().get(codeIndex);
            assertThat(code).matches("\\d{6}");
            assertThat(ttlCaptor.getAllValues().get(codeIndex)).isEqualTo(TTL);
        }

        @Test
        @DisplayName("initialise le compteur d'attempts à 0")
        void genererEtEnvoyer_init_attempts() {
            sut.genererEtEnvoyer(EMAIL, OtpChannel.EMAIL, DESTINATION);

            verify(cachePort).put(eq("otp:attempts:" + EMAIL), eq("0"), eq(TTL));
        }

        @Test
        @DisplayName("stocke le cooldown anti-spam")
        void genererEtEnvoyer_stocke_cooldown() {
            sut.genererEtEnvoyer(EMAIL, OtpChannel.EMAIL, DESTINATION);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(cachePort, atLeastOnce()).put(keyCaptor.capture(), anyString(), any(Duration.class));
            assertThat(keyCaptor.getAllValues()).contains("otp:cooldown:" + EMAIL);
        }

        @Test
        @DisplayName("appelle le sender avec le canal, la destination et un code 6 chiffres")
        void genererEtEnvoyer_appelle_sender() {
            sut.genererEtEnvoyer(EMAIL, OtpChannel.EMAIL, DESTINATION);

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(otpSenderPort).send(eq(OtpChannel.EMAIL), eq(DESTINATION), codeCaptor.capture());
            assertThat(codeCaptor.getValue()).matches("\\d{6}");
        }

        @Test
        @DisplayName("cooldown actif → OtpCooldownException sans envoyer de code")
        void genererEtEnvoyer_cooldown_actif_leve_exception() {
            long futur = System.currentTimeMillis() + 50_000;
            when(cachePort.get("otp:cooldown:" + EMAIL)).thenReturn(Optional.of(String.valueOf(futur)));

            assertThatThrownBy(() -> sut.genererEtEnvoyer(EMAIL, OtpChannel.EMAIL, DESTINATION))
                    .isInstanceOf(OtpCooldownException.class)
                    .hasMessageContaining("seconde");

            verifyNoInteractions(otpSenderPort);
            verify(cachePort, never()).put(eq("otp:code:" + EMAIL), anyString(), any());
        }

        @Test
        @DisplayName("cooldown expiré (timestamp passé) → envoi autorisé")
        void genererEtEnvoyer_cooldown_expire_autorise() {
            long passe = System.currentTimeMillis() - 1000;
            when(cachePort.get("otp:cooldown:" + EMAIL)).thenReturn(Optional.of(String.valueOf(passe)));

            assertThatNoException()
                    .isThrownBy(() -> sut.genererEtEnvoyer(EMAIL, OtpChannel.EMAIL, DESTINATION));

            verify(otpSenderPort).send(any(), any(), any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // valider
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("valider()")
    class Valider {

        private static final String CODE = "123456";

        @Test
        @DisplayName("code correct → évince le code et les attempts du cache")
        void valider_code_correct_evince_cache() {
            when(cachePort.get("otp:code:" + EMAIL)).thenReturn(Optional.of(CODE));
            when(cachePort.get("otp:attempts:" + EMAIL)).thenReturn(Optional.of("0"));

            sut.valider(EMAIL, CODE);

            verify(cachePort).evict("otp:code:" + EMAIL);
            verify(cachePort).evict("otp:attempts:" + EMAIL);
        }

        @Test
        @DisplayName("aucun code en cache (expiré) → OtpExpireException")
        void valider_code_expire_leve_exception() {
            when(cachePort.get("otp:code:" + EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.valider(EMAIL, CODE))
                    .isInstanceOf(OtpExpireException.class);
        }

        @Test
        @DisplayName("code incorrect → OtpInvalideException + incrémente les attempts")
        void valider_code_incorrect_incremente_attempts() {
            when(cachePort.get("otp:code:" + EMAIL)).thenReturn(Optional.of("999999"));
            when(cachePort.get("otp:attempts:" + EMAIL)).thenReturn(Optional.of("0"));

            assertThatThrownBy(() -> sut.valider(EMAIL, CODE))
                    .isInstanceOf(OtpInvalideException.class);

            verify(cachePort).put(eq("otp:attempts:" + EMAIL), eq("1"), eq(TTL));
        }

        @Test
        @DisplayName("nombre maximal de tentatives atteint → OtpTentativesEpuiseesException + evict")
        void valider_max_attempts_leve_exception_et_evince() {
            when(cachePort.get("otp:code:" + EMAIL)).thenReturn(Optional.of(CODE));
            when(cachePort.get("otp:attempts:" + EMAIL))
                    .thenReturn(Optional.of(String.valueOf(MAX_ATTEMPTS)));

            assertThatThrownBy(() -> sut.valider(EMAIL, CODE))
                    .isInstanceOf(OtpTentativesEpuiseesException.class);

            verify(cachePort).evict("otp:code:" + EMAIL);
            verify(cachePort).evict("otp:attempts:" + EMAIL);
        }

        @Test
        @DisplayName("comparaison à temps constant — codes de longueurs différentes → OtpInvalideException")
        void valider_longueurs_differentes_leve_exception() {
            when(cachePort.get("otp:code:" + EMAIL)).thenReturn(Optional.of("123456"));
            when(cachePort.get("otp:attempts:" + EMAIL)).thenReturn(Optional.of("0"));

            // Code court ≠ code long : ne doit pas lever d'IndexOutOfBounds
            assertThatThrownBy(() -> sut.valider(EMAIL, "12345"))
                    .isInstanceOf(OtpInvalideException.class);
        }

        @Test
        @DisplayName("attempts absent en cache → traité comme 0")
        void valider_attempts_absent_traite_comme_zero() {
            when(cachePort.get("otp:code:" + EMAIL)).thenReturn(Optional.of(CODE));
            when(cachePort.get("otp:attempts:" + EMAIL)).thenReturn(Optional.empty());

            // Ne doit pas lever d'exception : 0 < MAX_ATTEMPTS
            sut.valider(EMAIL, CODE);

            verify(cachePort).evict("otp:code:" + EMAIL);
        }
    }
}
