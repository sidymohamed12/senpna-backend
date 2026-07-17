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

    private StructureSanitaireJpaEntity(Builder builder) {
        super(builder.id);
        this.code = builder.code;
        this.nom = builder.nom;
        this.type = builder.type;
        this.regionId = builder.regionId;
        this.praId = builder.praId;
        this.district = builder.district;
        this.adresse = builder.adresse;
        this.telephone = builder.telephone;
        this.email = builder.email;
        this.responsableNom = builder.responsableNom;
        this.responsablePrenom = builder.responsablePrenom;
        this.statutAdhesion = builder.statutAdhesion;
        this.motifRejet = builder.motifRejet;
        this.actif = builder.actif;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String code;
        private String nom;
        private TypeStructureSanitaire type;
        private UUID regionId;
        private UUID praId;
        private String district;
        private String adresse;
        private String telephone;
        private String email;
        private String responsableNom;
        private String responsablePrenom;
        private StatutAdhesion statutAdhesion;
        private String motifRejet;
        private boolean actif;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder type(TypeStructureSanitaire type) {
            this.type = type;
            return this;
        }

        public Builder regionId(UUID regionId) {
            this.regionId = regionId;
            return this;
        }

        public Builder praId(UUID praId) {
            this.praId = praId;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder adresse(String adresse) {
            this.adresse = adresse;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder responsableNom(String responsableNom) {
            this.responsableNom = responsableNom;
            return this;
        }

        public Builder responsablePrenom(String responsablePrenom) {
            this.responsablePrenom = responsablePrenom;
            return this;
        }

        public Builder statutAdhesion(StatutAdhesion statutAdhesion) {
            this.statutAdhesion = statutAdhesion;
            return this;
        }

        public Builder motifRejet(String motifRejet) {
            this.motifRejet = motifRejet;
            return this;
        }

        public Builder actif(boolean actif) {
            this.actif = actif;
            return this;
        }

        public StructureSanitaireJpaEntity build() {
            return new StructureSanitaireJpaEntity(this);
        }
    }

}
