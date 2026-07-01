package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.exception.OtpCooldownException;
import ministere.sante.senpna.auth.domain.exception.OtpExpireException;
import ministere.sante.senpna.auth.domain.exception.OtpInvalideException;
import ministere.sante.senpna.auth.domain.exception.OtpTentativesEpuiseesException;
import ministere.sante.senpna.auth.domain.port.out.OtpSenderPort;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;

/**
 * Service applicatif — cycle de vie complet d'un code OTP (génération,
 * envoi multi-canal, validation, anti-brute-force, cooldown anti-spam).
 *
 * <h3>Stockage</h3>
 * <p>
 * Aucune table SQL dédiée : le code OTP est volatil par nature (TTL de
 * quelques minutes), donc porté entièrement par {@link CachePort}
 * (Redis en dev/prod, in-memory en test). Familles de clés :
 * </p>
 * <ul>
 * <li>{@code otp:code:&lt;email&gt;} — code en clair, TTL =
 * {@code app.otp.ttl}</li>
 * <li>{@code otp:attempts:&lt;email&gt;} — compteur de tentatives de
 * saisie</li>
 * <li>{@code otp:cooldown:&lt;email&gt;} — anti-spam entre deux envois</li>
 * </ul>
 * <p>
 * La gestion du jeton de réinitialisation après validation OTP est
 * déléguée à {@code ResetTokenPort} ({@code RedisResetTokenAdapter}) —
 * séparation des responsabilités, préfixe Redis distinct
 * ({@code auth:reset:*}).
 * </p>
 */
@Service
public class OtpService {

    private static final String PREFIX_CODE = "otp:code:";
    private static final String PREFIX_ATTEMPTS = "otp:attempts:";
    private static final String PREFIX_COOLDOWN = "otp:cooldown:";

    private final CachePort cachePort;
    private final OtpSenderPort otpSenderPort;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(CachePort cachePort, OtpSenderPort otpSenderPort, AppProperties appProperties) {
        this.cachePort = cachePort;
        this.otpSenderPort = otpSenderPort;
        this.appProperties = appProperties;
    }

    /**
     * Génère un nouveau code OTP, l'envoie via le canal choisi et démarre
     * la fenêtre de cooldown anti-spam.
     *
     * @throws OtpCooldownException si une demande récente est encore en cooldown
     */
    public void genererEtEnvoyer(String email, OtpChannel channel, String destination) {
        verifierCooldown(email);

        String code = genererCode();
        Duration ttl = appProperties.otp().ttl();

        cachePort.put(PREFIX_CODE + email, code, ttl);
        cachePort.put(PREFIX_ATTEMPTS + email, "0", ttl);

        Duration cooldown = appProperties.otp().cooldown();
        long cooldownEndEpochMs = System.currentTimeMillis() + cooldown.toMillis();
        cachePort.put(PREFIX_COOLDOWN + email, String.valueOf(cooldownEndEpochMs), cooldown);

        otpSenderPort.send(channel, destination, code);
    }

    /**
     * Valide le code soumis. En cas de succès, consomme le code (usage unique).
     * La génération du jeton de réinitialisation est déléguée à l'appelant
     * ({@code VerifyOtpUseCaseImpl}) via {@code ResetTokenPort}.
     *
     */
    public void valider(String email, String codeSoumis) {
        String codeAttendu = cachePort.get(PREFIX_CODE + email)
                .orElseThrow(OtpExpireException::new);

        int tentatives = Integer.parseInt(cachePort.get(PREFIX_ATTEMPTS + email).orElse("0"));
        int maxTentatives = appProperties.otp().maxAttempts();

        if (tentatives >= maxTentatives) {
            evincerCode(email);
            throw new OtpTentativesEpuiseesException();
        }

        if (!constantTimeEquals(codeAttendu, codeSoumis)) {
            cachePort.put(PREFIX_ATTEMPTS + email, String.valueOf(tentatives + 1), appProperties.otp().ttl());
            throw new OtpInvalideException();
        }

        evincerCode(email);
    }

    // ── Helpers privés ───────────────────────────────────────────────────

    private void verifierCooldown(String email) {
        Optional<String> cooldown = cachePort.get(PREFIX_COOLDOWN + email);
        if (cooldown.isEmpty()) {
            return;
        }
        long endEpochMs = Long.parseLong(cooldown.get());
        long remainingSec = (endEpochMs - System.currentTimeMillis()) / 1000;
        if (remainingSec > 0) {
            throw new OtpCooldownException(remainingSec);
        }
    }

    private void evincerCode(String email) {
        cachePort.evict(PREFIX_CODE + email);
        cachePort.evict(PREFIX_ATTEMPTS + email);
    }

    private String genererCode() {
        int length = appProperties.otp().length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Comparaison à temps constant — évite les attaques par timing sur la
     * validation du code OTP.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
