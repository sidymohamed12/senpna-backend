package ministere.sante.senpna.shared.infrastructure.security;

import java.util.UUID;

/**
 * Abstraction du principal actuellement authentifié, exposée via
 * {@code shared} afin qu'aucune feature (ex. {@code utilisateurs}) n'ait
 * besoin de dépendre des classes de sécurité internes de la feature
 * {@code auth} (ex. {@code AuthUserPrincipal}) pour obtenir l'identité de
 * l'acteur courant.
 *
 * <p>
 * L'implémentation concrète (portée par {@code auth}) est posée dans le
 * {@link org.springframework.security.core.Authentication#getPrincipal()}
 * de Spring Security ; les autres features se contentent de caster vers
 * cette interface, jamais vers un type concret d'une autre feature.
 * </p>
 */
public interface CurrentUser {

    UUID getUserId();
}
