package ministere.sante.senpna.shared.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Code technique utilisé dans Spring Security.
     * Format convention : {@code SNAKE_UPPER_CASE}, ex: {@code ADMIN_PNA}.
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    protected RoleJpaEntity() {
        // JPA
    }

    public RoleJpaEntity(UUID id, String code, String nom) {
        this.id = id;
        this.code = code;
        this.nom = nom;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }
}
