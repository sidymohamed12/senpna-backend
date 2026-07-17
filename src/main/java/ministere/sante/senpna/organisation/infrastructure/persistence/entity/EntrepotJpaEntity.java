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

    private EntrepotJpaEntity(Builder builder) {
        super(builder.id);
        this.code = builder.code;
        this.nom = builder.nom;
        this.type = builder.type;
        this.regionId = builder.regionId;
        this.adresse = builder.adresse;
        this.telephone = builder.telephone;
        this.responsableUserId = builder.responsableUserId;
        this.actif = builder.actif;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String code;
        private String nom;
        private TypeEntrepot type;
        private UUID regionId;
        private String adresse;
        private String telephone;
        private UUID responsableUserId;
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

        public Builder type(TypeEntrepot type) {
            this.type = type;
            return this;
        }

        public Builder regionId(UUID regionId) {
            this.regionId = regionId;
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

        public Builder responsableUserId(UUID responsableUserId) {
            this.responsableUserId = responsableUserId;
            return this;
        }

        public Builder actif(boolean actif) {
            this.actif = actif;
            return this;
        }

        public EntrepotJpaEntity build() {
            return new EntrepotJpaEntity(this);
        }
    }

}
