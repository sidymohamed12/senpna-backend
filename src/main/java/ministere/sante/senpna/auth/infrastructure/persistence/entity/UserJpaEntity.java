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

    public UserJpaEntity(UUID id, String nom, String prenom, String email, String telephone, String passwordHash,
            boolean actif, Set<UUID> roleIds, int tentativesEchecConnexion, Instant verrouilleJusqua) {
        super(id);
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.passwordHash = passwordHash;
        this.actif = actif;
        this.roleIds = new HashSet<>(roleIds);
        this.tentativesEchecConnexion = tentativesEchecConnexion;
        this.verrouilleJusqua = verrouilleJusqua;
    }
}
