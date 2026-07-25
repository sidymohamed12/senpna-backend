package ministere.sante.senpna.auth.infrastructure.security;

import com.sidymohamed12.jwt.core.claims.JwtClaims;
import com.sidymohamed12.jwt.core.exception.JwtValidationException;
import com.sidymohamed12.jwt.core.token.JwtTokenService;
import com.sidymohamed12.jwt.core.token.JwtTokenSpec;
import com.sidymohamed12.jwt.spring.autoconfigure.JwtProperties;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Adapter {@link TokenPort} basé sur jwt-toolkit (io.github.sidymohamed12).
 * <p>
 * Contrat inchangé pour le reste de l'application — {@code AuthTokenFactory},
 * {@code LogoutUseCaseImpl}, {@code RefreshTokenUseCaseImpl}... continuent de
 * dépendre uniquement de {@link TokenPort}, sans rien savoir de jwt-toolkit.
 * Seul cet adapter change : avant, il enveloppait un {@code JwtService}
 * maison + de la logique Redis en dur ; maintenant, il délègue à
 * {@link JwtTokenService} (génération/validation) et au
 * {@link RedisTokenRevocationPort} (révocation, déjà branché
 * automatiquement dans {@code JwtTokenService} par l'auto-configuration).
 */
@Component
public class JwtTokenAdapter implements TokenPort {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenAdapter.class);

    private final JwtTokenService jwtTokenService;
    private final RedisTokenRevocationPort revocationPort;
    private final JwtProperties jwtProperties;

    public JwtTokenAdapter(JwtTokenService jwtTokenService, RedisTokenRevocationPort revocationPort,
            JwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.revocationPort = revocationPort;
        this.jwtProperties = jwtProperties;
    }

    // ── Génération ────────────────────────────────────────────────────────

    @Override
    public String genererAccess(User user, Set<String> roleCodes, UUID entrepotId, UUID structureSanitaireId,
            UUID fournisseurId) {
        JwtTokenSpec spec = JwtTokenSpec.builder()
                .subject(user.getEmail().value())
                .ttl(jwtProperties.accessTokenTtl())
                .claim("roles", roleCodes)
                .claim("entrepotId", entrepotId)
                .claim("structureSanitaireId", structureSanitaireId)
                .claim("fournisseurId", fournisseurId)
                .build();
        return jwtTokenService.generate(spec);
    }

    @Override
    public String genererRefresh(User user) {
        JwtTokenSpec spec = JwtTokenSpec.builder()
                .subject(user.getEmail().value())
                .ttl(jwtProperties.refreshTokenTtl())
                .claim("type", "refresh")
                .build();
        return jwtTokenService.generate(spec);
    }

    // ── Révocation ────────────────────────────────────────────────────────

    @Override
    public void invalider(String token) {
        try {
            JwtClaims claims = jwtTokenService.parse(token);
            Duration ttlResiduelle = Duration.between(Instant.now(), claims.expiration());
            revocationPort.revoke(token, ttlResiduelle);
        } catch (JwtValidationException e) {
            // Token déjà invalide/expiré/malformé : révocation inutile, jamais bloquant.
            log.debug("[TokenRevocation] Révocation ignorée pour un token invalide : {}", e.getMessage());
        }
    }

    @Override
    public boolean estInvalide(String token) {
        // isValid() vérifie déjà signature + expiration + révocation en un seul appel
        // (le RedisTokenRevocationPort est consulté automatiquement par jwt-core).
        return !jwtTokenService.isValid(token);
    }

    // ── Extraction ────────────────────────────────────────────────────────

    @Override
    public String extraireEmail(String token) {
        return jwtTokenService.parse(token).subject();
    }

    @Override
    public boolean estRefreshToken(String token) {
        try {
            return jwtTokenService.parse(token).getString("type").filter("refresh"::equals).isPresent();
        } catch (JwtValidationException e) {
            return false;
        }
    }

    @Override
    public boolean estExpire(String token) {
        return jwtTokenService.isExpired(token);
    }
}