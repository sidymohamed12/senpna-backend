package ministere.sante.senpna.auth.infrastructure.persistence.mapper;

import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.valueobject.HashedPassword;
import ministere.sante.senpna.auth.domain.valueobject.Nom;
import ministere.sante.senpna.auth.domain.valueobject.Phone;
import ministere.sante.senpna.auth.domain.valueobject.Prenom;
import ministere.sante.senpna.auth.domain.valueobject.UserId;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserJpaEntity entity) {
        return User.reconstruct(
                UserId.of(entity.getId()),
                Nom.of(entity.getNom()),
                Prenom.of(entity.getPrenom()),
                Email.of(entity.getEmail()),
                entity.getTelephone() != null ? Phone.of(entity.getTelephone()) : null,
                HashedPassword.of(entity.getPasswordHash()),
                entity.isActif(),
                entity.getRoleIds(),
                entity.getTentativesEchecConnexion(),
                entity.getVerrouilleJusqua(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getId().getValue(),
                user.getNom().getValue(),
                user.getPrenom().getValue(),
                user.getEmail().value(),
                user.getTelephone() != null ? user.getTelephone().value() : null,
                user.getHashedPassword().value(),
                user.isActif(),
                user.getRoleIds(),
                user.getTentativesEchecConnexion(),
                user.getVerrouilleJusqua());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}
