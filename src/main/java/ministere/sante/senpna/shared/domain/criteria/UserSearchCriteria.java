package ministere.sante.senpna.shared.domain.criteria;

import java.util.UUID;

public record UserSearchCriteria(String recherche, Boolean actif, UUID roleId) {

    public static UserSearchCriteria vide() {
        return new UserSearchCriteria(null, null, null);
    }
}
