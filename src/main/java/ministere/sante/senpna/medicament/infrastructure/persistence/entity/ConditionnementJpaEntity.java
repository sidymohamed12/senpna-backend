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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "conditionnements")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class ConditionnementJpaEntity extends BaseJpaEntity {

    @Column(name = "medicament_id", nullable = false)
    private UUID medicamentId;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "niveau", nullable = false)
    private int niveau;

    @Column(name = "quantite_unite_base", nullable = false, precision = 14, scale = 4)
    private BigDecimal quantiteUniteBase;

    @Column(name = "est_unite_base", nullable = false)
    private boolean estUniteBase;

    @Column(name = "prix_achat", precision = 14, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "prix_vente", precision = 14, scale = 2)
    private BigDecimal prixVente;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    private ConditionnementJpaEntity(Builder builder) {
        super(builder.id);
        this.medicamentId = builder.medicamentId;
        this.nom = builder.nom;
        this.niveau = builder.niveau;
        this.quantiteUniteBase = builder.quantiteUniteBase;
        this.estUniteBase = builder.estUniteBase;
        this.prixAchat = builder.prixAchat;
        this.prixVente = builder.prixVente;
        this.actif = builder.actif;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private UUID medicamentId;
        private String nom;
        private int niveau;
        private BigDecimal quantiteUniteBase;
        private boolean estUniteBase;
        private BigDecimal prixAchat;
        private BigDecimal prixVente;
        private boolean actif;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder medicamentId(UUID medicamentId) {
            this.medicamentId = medicamentId;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder niveau(int niveau) {
            this.niveau = niveau;
            return this;
        }

        public Builder quantiteUniteBase(BigDecimal quantiteUniteBase) {
            this.quantiteUniteBase = quantiteUniteBase;
            return this;
        }

        public Builder estUniteBase(boolean estUniteBase) {
            this.estUniteBase = estUniteBase;
            return this;
        }

        public Builder prixAchat(BigDecimal prixAchat) {
            this.prixAchat = prixAchat;
            return this;
        }

        public Builder prixVente(BigDecimal prixVente) {
            this.prixVente = prixVente;
            return this;
        }

        public Builder actif(boolean actif) {
            this.actif = actif;
            return this;
        }

        public ConditionnementJpaEntity build() {
            return new ConditionnementJpaEntity(this);
        }
    }

}
