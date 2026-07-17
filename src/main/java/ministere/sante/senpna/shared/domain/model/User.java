package ministere.sante.senpna.shared.domain.model;

import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class User extends AggregateRoot<UserId> {

    private Nom nom;
    private Prenom prenom;
    private final Email email;
    private Phone telephone; // optionnel — requis uniquement pour le canal OTP SMS
    private HashedPassword hashedPassword;
    private boolean actif;
    private final Set<UUID> roleIds;
    private int tentativesEchecConnexion;
    private Instant verrouilleJusqua;

    private User(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.nom = Objects.requireNonNull(builder.nom, "Le nom ne peut pas être null");
        this.prenom = Objects.requireNonNull(builder.prenom, "Le prénom ne peut pas être null");
        this.email = Objects.requireNonNull(builder.email, "L'email ne peut pas être null");
        this.telephone = builder.telephone;
        this.hashedPassword = Objects.requireNonNull(builder.hashedPassword, "Le mot de passe ne peut pas être null");
        this.actif = builder.actif;
        this.roleIds = new HashSet<>(Objects.requireNonNull(builder.roleIds, "roleIds ne peut pas être null"));
        this.tentativesEchecConnexion = builder.tentativesEchecConnexion;
        this.verrouilleJusqua = builder.verrouilleJusqua;
    }

    /** Données nécessaires à la création d'un nouvel utilisateur. */
    public record CreationCommand(Nom nom, Prenom prenom, Email email, Phone telephone,
            HashedPassword hashedPassword, Set<UUID> roleIds) {
    }

    public static User creer(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(UserId.generate())
                .nom(command.nom())
                .prenom(command.prenom())
                .email(command.email())
                .telephone(command.telephone())
                .hashedPassword(command.hashedPassword())
                .actif(true)
                .roleIds(command.roleIds())
                .tentativesEchecConnexion(0)
                .verrouilleJusqua(null)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UserId id;
        private Nom nom;
        private Prenom prenom;
        private Email email;
        private Phone telephone;
        private HashedPassword hashedPassword;
        private boolean actif;
        private Set<UUID> roleIds;
        private int tentativesEchecConnexion;
        private Instant verrouilleJusqua;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(UserId id) {
            this.id = id;
            return this;
        }

        public Builder nom(Nom nom) {
            this.nom = nom;
            return this;
        }

        public Builder prenom(Prenom prenom) {
            this.prenom = prenom;
            return this;
        }

        public Builder email(Email email) {
            this.email = email;
            return this;
        }

        public Builder telephone(Phone telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder hashedPassword(HashedPassword hashedPassword) {
            this.hashedPassword = hashedPassword;
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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void changerMotDePasse(HashedPassword nouveauMotDePasse) {
        this.hashedPassword = Objects.requireNonNull(nouveauMotDePasse);
        this.tentativesEchecConnexion = 0;
        this.verrouilleJusqua = null;
        markUpdated();
    }

    public void renommer(Nom nouveauNom, Prenom nouveauPrenom) {
        this.nom = Objects.requireNonNull(nouveauNom);
        this.prenom = Objects.requireNonNull(nouveauPrenom);
        markUpdated();
    }

    public void modifierTelephone(Phone nouveauTelephone) {
        this.telephone = nouveauTelephone;
        markUpdated();
    }

    public void activer() {
        if (this.actif) {
            return;
        }
        this.actif = true;
        markUpdated();
    }

    public void desactiver() {
        if (!this.actif) {
            return;
        }
        this.actif = false;
        markUpdated();
    }

    public boolean estVerrouille() {
        return verrouilleJusqua != null && Instant.now().isBefore(verrouilleJusqua);
    }

    public void enregistrerEchecConnexion(int seuilVerrouillage, Instant verrouillageJusqua) {
        this.tentativesEchecConnexion++;
        if (this.tentativesEchecConnexion >= seuilVerrouillage) {
            this.verrouilleJusqua = verrouillageJusqua;
        }
        markUpdated();
    }

    public void reinitialiserEchecsConnexion() {
        this.tentativesEchecConnexion = 0;
        this.verrouilleJusqua = null;
        markUpdated();
    }

    public void ajouterRole(UUID roleId) {
        this.roleIds.add(Objects.requireNonNull(roleId));
        markUpdated();
    }

    public void retirerRole(UUID roleId) {
        this.roleIds.remove(roleId);
        markUpdated();
    }

    public boolean possedeAuMoinsUnRole() {
        return !roleIds.isEmpty();
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public Nom getNom() {
        return nom;
    }

    public Prenom getPrenom() {
        return prenom;
    }

    public Email getEmail() {
        return email;
    }

    public Phone getTelephone() {
        return telephone;
    }

    public HashedPassword getHashedPassword() {
        return hashedPassword;
    }

    public boolean isActif() {
        return actif;
    }

    public Set<UUID> getRoleIds() {
        return Collections.unmodifiableSet(roleIds);
    }

    public int getTentativesEchecConnexion() {
        return tentativesEchecConnexion;
    }

    public Instant getVerrouilleJusqua() {
        return verrouilleJusqua;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((nom == null) ? 0 : nom.hashCode());
        result = prime * result + ((prenom == null) ? 0 : prenom.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((telephone == null) ? 0 : telephone.hashCode());
        result = prime * result + ((hashedPassword == null) ? 0 : hashedPassword.hashCode());
        result = prime * result + (actif ? 1231 : 1237);
        result = prime * result + ((roleIds == null) ? 0 : roleIds.hashCode());
        result = prime * result + tentativesEchecConnexion;
        result = prime * result + ((verrouilleJusqua == null) ? 0 : verrouilleJusqua.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (nom == null) {
            if (other.nom != null)
                return false;
        } else if (!nom.equals(other.nom))
            return false;
        if (prenom == null) {
            if (other.prenom != null)
                return false;
        } else if (!prenom.equals(other.prenom))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (telephone == null) {
            if (other.telephone != null)
                return false;
        } else if (!telephone.equals(other.telephone))
            return false;
        if (hashedPassword == null) {
            if (other.hashedPassword != null)
                return false;
        } else if (!hashedPassword.equals(other.hashedPassword))
            return false;
        if (actif != other.actif)
            return false;
        if (roleIds == null) {
            if (other.roleIds != null)
                return false;
        } else if (!roleIds.equals(other.roleIds))
            return false;
        if (tentativesEchecConnexion != other.tentativesEchecConnexion)
            return false;
        if (verrouilleJusqua == null) {
            if (other.verrouilleJusqua != null)
                return false;
        } else if (!verrouilleJusqua.equals(other.verrouilleJusqua))
            return false;
        return true;
    }
}
