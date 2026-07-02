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
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "structures_sanitaires")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class StructureSanitaireJpaEntity extends BaseJpaEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TypeStructureSanitaire type;

    @Column(name = "region_id")
    private UUID regionId;

    @Column(name = "pra_id")
    private UUID praId;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "responsable_nom", length = 100)
    private String responsableNom;

    @Column(name = "responsable_prenom", length = 100)
    private String responsablePrenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_adhesion", nullable = false, length = 30)
    private StatutAdhesion statutAdhesion;

    @Column(name = "motif_rejet", length = 255)
    private String motifRejet;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    public StructureSanitaireJpaEntity(UUID id, String code, String nom, TypeStructureSanitaire type, UUID regionId,
            UUID praId, String district, String adresse, String telephone, String email, String responsableNom,
            String responsablePrenom, StatutAdhesion statutAdhesion, String motifRejet, boolean actif) {
        super(id);
        this.code = code;
        this.nom = nom;
        this.type = type;
        this.regionId = regionId;
        this.praId = praId;
        this.district = district;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.responsableNom = responsableNom;
        this.responsablePrenom = responsablePrenom;
        this.statutAdhesion = statutAdhesion;
        this.motifRejet = motifRejet;
        this.actif = actif;
    }
}
