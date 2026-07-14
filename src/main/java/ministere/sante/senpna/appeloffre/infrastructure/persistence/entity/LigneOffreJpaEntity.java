package ministere.sante.senpna.appeloffre.infrastructure.persistence.entity;

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
@Table(name = "offre_lignes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class LigneOffreJpaEntity extends BaseJpaEntity {

    @Column(name = "offre_id", nullable = false)
    private UUID offreId;

    @Column(name = "ligne_appel_offre_id", nullable = false)
    private UUID ligneAppelOffreId;

    @Column(name = "prix_unitaire", nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "delai_livraison_jours", nullable = false)
    private Integer delaiLivraisonJours;

    public LigneOffreJpaEntity(UUID id, UUID offreId, UUID ligneAppelOffreId, BigDecimal prixUnitaire,
            Integer delaiLivraisonJours) {
        super(id);
        this.offreId = offreId;
        this.ligneAppelOffreId = ligneAppelOffreId;
        this.prixUnitaire = prixUnitaire;
        this.delaiLivraisonJours = delaiLivraisonJours;
    }
}
