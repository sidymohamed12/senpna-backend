package ministere.sante.senpna.auth.infrastructure.web.dto.request;

/**
 * Le refresh token est optionnel : un client peut ne conserver que
 * l'access token (ou l'avoir déjà perdu). Dans tous les cas, l'access
 * token courant (porté par l'en-tête {@code Authorization}) est révoqué.
 */
public record LogoutRequest(String refreshToken) {
}
