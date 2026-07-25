package ministere.sante.senpna.auth.infrastructure.security;

import com.sidymohamed12.jwt.core.algorithm.JwtAlgorithm;
import com.sidymohamed12.jwt.core.claims.JwtClaims;
import com.sidymohamed12.jwt.core.exception.JwtValidationException;
import com.sidymohamed12.jwt.core.token.JwtTokenService;
import com.sidymohamed12.jwt.core.token.JwtTokenSpec;
import com.sidymohamed12.jwt.spring.autoconfigure.JwtProperties;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Migré vers jwt-toolkit : {@code JwtTokenAdapter} délègue maintenant à
 * {@link JwtTokenService} (génération/validation) et à
 * {@link RedisTokenRevocationPort} (révocation — dont la logique
 * SHA-256/Redis est testée séparément dans {@code RedisTokenRevocationPortTest},
 * plus dans cette classe).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenAdapter")
class JwtTokenAdapterTest {

    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private RedisTokenRevocationPort revocationPort;

    private JwtTokenAdapter sut;
    private User user;

    @BeforeEach
    void setup() {
        user = UserFixtures.actif();
        JwtProperties jwtProperties = new JwtProperties(
                JwtAlgorithm.HS256, "peu-importe-ici", null, null,
                Duration.ofMinutes(15), Duration.ofDays(7));
        sut = new JwtTokenAdapter(jwtTokenService, revocationPort, jwtProperties);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Génération
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("genererAccess()")
    class GenererAccess {

        @Test
        @DisplayName("délègue à JwtTokenService avec l'email en subject, le TTL configuré et le claim 'roles'")
        void genererAccess_sans_affectation_delegue() {
            Set<String> roleCodes = Set.of("GESTIONNAIRE_PNA", "PHARMACIEN_PRA");
            ArgumentCaptor<JwtTokenSpec> specCaptor = ArgumentCaptor.forClass(JwtTokenSpec.class);
            when(jwtTokenService.generate(any())).thenReturn("access.jwt");

            String token = sut.genererAccess(user, roleCodes, null, null, null);

            assertThat(token).isEqualTo("access.jwt");
            verify(jwtTokenService).generate(specCaptor.capture());
            JwtTokenSpec spec = specCaptor.getValue();
            assertThat(spec.subject()).isEqualTo(UserFixtures.EMAIL);
            assertThat(spec.ttl()).isEqualTo(Duration.ofMinutes(15));
            assertThat(spec.claims()).containsEntry("roles", roleCodes);
            assertThat(spec.claims()).doesNotContainKeys("entrepotId", "structureSanitaireId", "fournisseurId");
        }

        @Test
        @DisplayName("inclut entrepotId et structureSanitaireId dans les claims quand fournis")
        void genererAccess_avec_affectation_inclut_les_claims() {
            UUID entrepotId = UUID.randomUUID();
            UUID structureSanitaireId = UUID.randomUUID();
            ArgumentCaptor<JwtTokenSpec> specCaptor = ArgumentCaptor.forClass(JwtTokenSpec.class);
            when(jwtTokenService.generate(any())).thenReturn("access.jwt");

            sut.genererAccess(user, Set.of("GESTIONNAIRE_PNA"), entrepotId, structureSanitaireId, null);

            verify(jwtTokenService).generate(specCaptor.capture());
            Map<String, Object> claims = specCaptor.getValue().claims();
            assertThat(claims).containsEntry("entrepotId", entrepotId.toString());
            assertThat(claims).containsEntry("structureSanitaireId", structureSanitaireId.toString());
            assertThat(claims).doesNotContainKey("fournisseurId");
        }

        @Test
        @DisplayName("entrepotId fourni seul → structureSanitaireId et fournisseurId absents des claims")
        void genererAccess_avec_entrepot_seul() {
            UUID entrepotId = UUID.randomUUID();
            ArgumentCaptor<JwtTokenSpec> specCaptor = ArgumentCaptor.forClass(JwtTokenSpec.class);
            when(jwtTokenService.generate(any())).thenReturn("access.jwt");

            sut.genererAccess(user, Set.of("GESTIONNAIRE_PNA"), entrepotId, null, null);

            verify(jwtTokenService).generate(specCaptor.capture());
            Map<String, Object> claims = specCaptor.getValue().claims();
            assertThat(claims).containsEntry("entrepotId", entrepotId.toString());
            assertThat(claims).doesNotContainKeys("structureSanitaireId", "fournisseurId");
        }

        @Test
        @DisplayName("fournisseurId fourni seul → inclus dans les claims, entrepotId/structureSanitaireId absents")
        void genererAccess_avec_fournisseur_seul() {
            UUID fournisseurId = UUID.randomUUID();
            ArgumentCaptor<JwtTokenSpec> specCaptor = ArgumentCaptor.forClass(JwtTokenSpec.class);
            when(jwtTokenService.generate(any())).thenReturn("access.jwt");

            sut.genererAccess(user, Set.of("FOURNISSEUR"), null, null, fournisseurId);

            verify(jwtTokenService).generate(specCaptor.capture());
            Map<String, Object> claims = specCaptor.getValue().claims();
            assertThat(claims).containsEntry("fournisseurId", fournisseurId.toString());
            assertThat(claims).doesNotContainKeys("entrepotId", "structureSanitaireId");
        }
    }

    @Nested
    @DisplayName("genererRefresh()")
    class GenererRefresh {

        @Test
        @DisplayName("délègue à JwtTokenService avec l'email en subject, le TTL refresh et le claim type=refresh")
        void genererRefresh_delegue() {
            ArgumentCaptor<JwtTokenSpec> specCaptor = ArgumentCaptor.forClass(JwtTokenSpec.class);
            when(jwtTokenService.generate(any())).thenReturn("refresh.jwt");

            String token = sut.genererRefresh(user);

            assertThat(token).isEqualTo("refresh.jwt");
            verify(jwtTokenService).generate(specCaptor.capture());
            JwtTokenSpec spec = specCaptor.getValue();
            assertThat(spec.subject()).isEqualTo(UserFixtures.EMAIL);
            assertThat(spec.ttl()).isEqualTo(Duration.ofDays(7));
            assertThat(spec.claims()).containsEntry("type", "refresh");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Révocation
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("invalider()")
    class Invalider {

        @Test
        @DisplayName("révoque via RedisTokenRevocationPort avec la TTL résiduelle jusqu'à expiration")
        void invalider_delegue_avec_ttl_residuelle() {
            String token = "header.payload.signature";
            Instant expiration = Instant.now().plusSeconds(300);
            when(jwtTokenService.parse(token))
                    .thenReturn(new JwtClaims("user@example.com", null, Instant.now(), expiration, Map.of()));

            sut.invalider(token);

            ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(revocationPort).revoke(eq(token), ttlCaptor.capture());
            assertThat(ttlCaptor.getValue().toSeconds()).isBetween(290L, 300L);
        }

        @Test
        @DisplayName("token invalide/malformé — ignore silencieusement, n'appelle jamais revoke()")
        void invalider_token_invalide_silencieux() {
            String token = "not.a.jwt";
            when(jwtTokenService.parse(token)).thenThrow(new JwtValidationException("malformé"));

            assertThatNoException().isThrownBy(() -> sut.invalider(token));
            verifyNoInteractions(revocationPort);
        }
    }

    @Nested
    @DisplayName("estInvalide()")
    class EstInvalide {

        @Test
        @DisplayName("jwtTokenService.isValid() = false → true (signature, expiration ou révocation)")
        void estInvalide_quand_service_dit_invalide() {
            when(jwtTokenService.isValid("token")).thenReturn(false);
            assertThat(sut.estInvalide("token")).isTrue();
        }

        @Test
        @DisplayName("jwtTokenService.isValid() = true → false")
        void estInvalide_quand_service_dit_valide() {
            when(jwtTokenService.isValid("token")).thenReturn(true);
            assertThat(sut.estInvalide("token")).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Extraction
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Extraction de claims")
    class Extraction {

        @Test
        @DisplayName("extraireEmail() renvoie le subject du token parsé")
        void extraireEmail_delegue() {
            when(jwtTokenService.parse("some.jwt")).thenReturn(new JwtClaims(
                    "user@example.com", null, Instant.now(), Instant.now().plusSeconds(300), Map.of()));

            assertThat(sut.extraireEmail("some.jwt")).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("estRefreshToken() → true si le claim 'type' vaut 'refresh'")
        void estRefreshToken_true_si_claim_refresh() {
            when(jwtTokenService.parse("refresh.jwt")).thenReturn(new JwtClaims(
                    "user@example.com", null, Instant.now(), Instant.now().plusSeconds(300),
                    Map.of("type", "refresh")));

            assertThat(sut.estRefreshToken("refresh.jwt")).isTrue();
        }

        @Test
        @DisplayName("estRefreshToken() → false si le claim 'type' est absent")
        void estRefreshToken_false_si_claim_absent() {
            when(jwtTokenService.parse("access.jwt")).thenReturn(new JwtClaims(
                    "user@example.com", null, Instant.now(), Instant.now().plusSeconds(300), Map.of()));

            assertThat(sut.estRefreshToken("access.jwt")).isFalse();
        }

        @Test
        @DisplayName("estRefreshToken() → false si le token est invalide (n'écarte pas l'exception)")
        void estRefreshToken_false_si_invalide() {
            when(jwtTokenService.parse("malformed")).thenThrow(new JwtValidationException("invalide"));

            assertThat(sut.estRefreshToken("malformed")).isFalse();
        }

        @Test
        @DisplayName("estExpire() délègue à jwtTokenService.isExpired()")
        void estExpire_delegue() {
            when(jwtTokenService.isExpired("token")).thenReturn(true);
            assertThat(sut.estExpire("token")).isTrue();

            when(jwtTokenService.isExpired("autre")).thenReturn(false);
            assertThat(sut.estExpire("autre")).isFalse();
        }
    }
}
