package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Délègue à {@link PasswordEncoder} (BCrypt strength=12, configuré dans
 * {@code SecurityConfig}). Les use cases ne dépendent jamais de Spring
 * Security directement — uniquement de {@link PasswordEncoderPort}.
 */
@Component
public class SpringPasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public SpringPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encoder(String motDePasseBrut) {
        return passwordEncoder.encode(motDePasseBrut);
    }

    @Override
    public boolean correspond(String motDePasseBrut, String hash) {
        return passwordEncoder.matches(motDePasseBrut, hash);
    }
}
