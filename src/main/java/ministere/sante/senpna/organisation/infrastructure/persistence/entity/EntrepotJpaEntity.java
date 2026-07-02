package ministere.sante.senpna.organisation.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "entrepots")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class EntrepotJpaEntity extends BaseJpaEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TypeEntrepot type;

    @Column(name = "region_id")
    private UUID regionId;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "responsable_user_id")
    private UUID responsableUserId;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    public EntrepotJpaEntity(UUID id, String code, String nom, TypeEntrepot type, UUID regionId, String adresse,
            String telephone, UUID responsableUserId, boolean actif) {
        super(id);
        this.code = code;
        this.nom = nom;
        this.type = type;
        this.regionId = regionId;
        this.adresse = adresse;
        this.telephone = telephone;
        this.responsableUserId = responsableUserId;
        this.actif = actif;
    }
}
