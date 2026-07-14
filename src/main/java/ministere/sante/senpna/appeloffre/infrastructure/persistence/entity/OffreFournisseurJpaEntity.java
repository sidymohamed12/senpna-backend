package ministere.sante.senpna.appeloffre.infrastructure.persistence.entity;

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

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "offres_fournisseurs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class OffreFournisseurJpaEntity extends BaseJpaEntity {

    @Column(name = "appel_offre_id", nullable = false)
    private UUID appelOffreId;

    @Column(name = "fournisseur_id", nullable = false)
    private UUID fournisseurId;

    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutOffre statut;

    public OffreFournisseurJpaEntity(UUID id, UUID appelOffreId, UUID fournisseurId, String commentaire,
            StatutOffre statut) {
        super(id);
        this.appelOffreId = appelOffreId;
        this.fournisseurId = fournisseurId;
        this.commentaire = commentaire;
        this.statut = statut;
    }
}
