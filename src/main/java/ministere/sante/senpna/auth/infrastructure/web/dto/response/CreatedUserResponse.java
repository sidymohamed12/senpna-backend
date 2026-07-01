package ministere.sante.senpna.auth.infrastructure.web.dto.response;

public record CreatedUserResponse(UserResponse user, String motDePasseTemporaire) {
}
