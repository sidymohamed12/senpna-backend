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

/**
 * Ligne d'un appel d'offres — persistée dans sa propre table, rattachée à
 * son parent par simple colonne {@code appel_offre_id} (pas de relation
 * JPA bidirectionnelle : l'agrégat {@code AppelOffre} est recomposé
 * explicitement par {@code AppelOffreRepositoryAdapter}, jamais par le
 * moteur ORM — cf. {@code LotJpaEntity} pour la même convention avec des
 * FK inter-agrégats).
 */
@Entity
@Table(name = "appel_offre_lignes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class LigneAppelOffreJpaEntity extends BaseJpaEntity {

    @Column(name = "appel_offre_id", nullable = false)
    private UUID appelOffreId;

    @Column(name = "medicament_id", nullable = false)
    private UUID medicamentId;

    @Column(name = "designation", nullable = false, length = 255)
    private String designation;

    @Column(name = "quantite_estimee", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantiteEstimee;

    @Column(name = "unite_base", nullable = false, length = 30)
    private String uniteBase;

    public LigneAppelOffreJpaEntity(UUID id, UUID appelOffreId, UUID medicamentId, String designation,
            BigDecimal quantiteEstimee, String uniteBase) {
        super(id);
        this.appelOffreId = appelOffreId;
        this.medicamentId = medicamentId;
        this.designation = designation;
        this.quantiteEstimee = quantiteEstimee;
        this.uniteBase = uniteBase;
    }
}
