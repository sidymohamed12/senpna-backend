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
        return Facture.reconstruct(
                FactureId.of(entity.getId()),
                CommandeAchatId.of(entity.getCommandeAchatId()),
                FournisseurId.of(entity.getFournisseurId()),
                entity.getNumeroFacture(),
                entity.getMontant(),
                entity.getDateEmission(),
                entity.getDateEcheance(),
                entity.getPieceJointeMediaId(),
                entity.getStatut(),
                entity.getMotifRejet(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public FactureJpaEntity toEntity(Facture facture) {
        FactureJpaEntity entity = new FactureJpaEntity(
                facture.getId().getValue(),
                facture.getCommandeAchatId().getValue(),
                facture.getFournisseurId().getValue(),
                facture.getNumeroFacture(),
                facture.getMontant(),
                facture.getDateEmission(),
                facture.getDateEcheance(),
                facture.getPieceJointeMediaId(),
                facture.getStatut(),
                facture.getMotifRejet());
        entity.setCreatedAt(facture.getCreatedAt());
        entity.setUpdatedAt(facture.getUpdatedAt());
        return entity;
    }
}
