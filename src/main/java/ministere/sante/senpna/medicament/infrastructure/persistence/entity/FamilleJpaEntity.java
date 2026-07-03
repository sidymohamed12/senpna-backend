package ministere.sante.senpna.medicament.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "familles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class FamilleJpaEntity extends BaseJpaEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "libelle", nullable = false, length = 150)
    private String libelle;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    public FamilleJpaEntity(UUID id, String code, String libelle, String description, boolean actif) {
        super(id);
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.actif = actif;
    }
}
