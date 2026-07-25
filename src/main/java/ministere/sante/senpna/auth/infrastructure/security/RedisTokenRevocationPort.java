package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.shared.domain.port.out.CachePort;

import com.sidymohamed12.jwt.core.revocation.TokenRevocationPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Component
public class RedisTokenRevocationPort implements TokenRevocationPort {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenRevocationPort.class);
    private static final String REVOCATION_PREFIX = "auth:revoked:";

    private final CachePort cachePort;

    public RedisTokenRevocationPort(CachePort cachePort) {
        this.cachePort = cachePort;
    }

    @Override
    public void revoke(String token, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            log.debug("[TokenRevocation] TTL résiduelle nulle/négative, révocation inutile.");
            return;
        }
        cachePort.put(REVOCATION_PREFIX + sha256(token), "1", ttl);
        log.info("[TokenRevocation] Token révoqué (TTL résiduelle={})", ttl);
    }

    @Override
    public boolean isRevoked(String token) {
        try {
            return cachePort.get(REVOCATION_PREFIX + sha256(token)).isPresent();
        } catch (Exception e) {
            // Fail-open volontaire : une panne Redis ne doit pas bloquer tous
            // les utilisateurs. À ajuster selon le niveau de sécurité requis.
            log.error("[TokenRevocation] Impossible de vérifier la révocation, fail-open : {}", e.getMessage());
            return false;
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti dans tout JDK — ne peut pas se produire
            throw new IllegalStateException("SHA-256 non disponible", e);
        }
    }
}
