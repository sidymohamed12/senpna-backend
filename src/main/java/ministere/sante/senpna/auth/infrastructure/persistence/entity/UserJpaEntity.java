package ministere.sante.senpna.auth.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class UserJpaEntity extends BaseJpaEntity {

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_id", nullable = false)
    private Set<UUID> roleIds = new HashSet<>();

    @Column(name = "tentatives_echec_connexion", nullable = false)
    private int tentativesEchecConnexion;

    @Column(name = "verrouille_jusqua")
    private Instant verrouilleJusqua;

    private UserJpaEntity(Builder builder) {
        super(builder.id);
        this.nom = builder.nom;
        this.prenom = builder.prenom;
        this.email = builder.email;
        this.telephone = builder.telephone;
        this.passwordHash = builder.passwordHash;
        this.actif = builder.actif;
        this.roleIds = new HashSet<>(builder.roleIds);
        this.tentativesEchecConnexion = builder.tentativesEchecConnexion;
        this.verrouilleJusqua = builder.verrouilleJusqua;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String passwordHash;
        private boolean actif;
        private Set<UUID> roleIds;
        private int tentativesEchecConnexion;
        private Instant verrouilleJusqua;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder prenom(String prenom) {
            this.prenom = prenom;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder actif(boolean actif) {
            this.actif = actif;
            return this;
        }

        public Builder roleIds(Set<UUID> roleIds) {
            this.roleIds = roleIds;
            return this;
        }

        public Builder tentativesEchecConnexion(int tentativesEchecConnexion) {
            this.tentativesEchecConnexion = tentativesEchecConnexion;
            return this;
        }

        public Builder verrouilleJusqua(Instant verrouilleJusqua) {
            this.verrouilleJusqua = verrouilleJusqua;
            return this;
        }

        public UserJpaEntity build() {
            return new UserJpaEntity(this);
        }
    }
}
