package ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper;

import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.FactureJpaEntity;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Component;

@Component
public class FactureMapper {

    public Facture toDomain(FactureJpaEntity entity) {
        return Facture.builder()
            .id(FactureId.of(entity.getId()))
            .commandeAchatId(CommandeAchatId.of(entity.getCommandeAchatId()))
            .fournisseurId(FournisseurId.of(entity.getFournisseurId()))
            .numeroFacture(entity.getNumeroFacture())
            .montant(entity.getMontant())
            .dateEmission(entity.getDateEmission())
            .dateEcheance(entity.getDateEcheance())
            .pieceJointeMediaId(entity.getPieceJointeMediaId())
            .statut(entity.getStatut())
            .motifRejet(entity.getMotifRejet())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public FactureJpaEntity toEntity(Facture facture) {
        FactureJpaEntity entity = FactureJpaEntity.builder()
            .id(facture.getId().getValue())
            .commandeAchatId(facture.getCommandeAchatId().getValue())
            .fournisseurId(facture.getFournisseurId().getValue())
            .numeroFacture(facture.getNumeroFacture())
            .montant(facture.getMontant())
            .dateEmission(facture.getDateEmission())
            .dateEcheance(facture.getDateEcheance())
            .pieceJointeMediaId(facture.getPieceJointeMediaId())
            .statut(facture.getStatut())
            .motifRejet(facture.getMotifRejet())
            .build();
        entity.setCreatedAt(facture.getCreatedAt());
        entity.setUpdatedAt(facture.getUpdatedAt());
        return entity;
    }
}
