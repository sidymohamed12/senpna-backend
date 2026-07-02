package ministere.sante.senpna.fournisseur.infrastructure.persistence.entity;

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
@Table(name = "fournisseurs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class FournisseurJpaEntity extends BaseJpaEntity {

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "contact_principal", length = 150)
    private String contactPrincipal;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    public FournisseurJpaEntity(UUID id, String nom, String adresse, String telephone, String email,
            String contactPrincipal, boolean actif) {
        super(id);
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.contactPrincipal = contactPrincipal;
        this.actif = actif;
    }
}
