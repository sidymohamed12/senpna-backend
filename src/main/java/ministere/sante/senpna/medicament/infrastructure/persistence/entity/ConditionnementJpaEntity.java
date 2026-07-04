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

    public ConditionnementJpaEntity(UUID id, UUID medicamentId, String nom, int niveau,
            BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat, BigDecimal prixVente,
            boolean actif) {
        super(id);
        this.medicamentId = medicamentId;
        this.nom = nom;
        this.niveau = niveau;
        this.quantiteUniteBase = quantiteUniteBase;
        this.estUniteBase = estUniteBase;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.actif = actif;
    }
}
