package ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper;

import ministere.sante.senpna.commandeachat.domain.model.AvisExpedition;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.LigneCommandeAchatId;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.CommandeAchatJpaEntity;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.LigneCommandeAchatJpaEntity;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandeAchatMapper {

    public CommandeAchat toDomain(CommandeAchatJpaEntity entity, List<LigneCommandeAchatJpaEntity> lignesEntity) {
        List<LigneCommandeAchat> lignes = lignesEntity.stream().map(this::toDomainLigne).toList();

        AvisExpedition avis = entity.getAvisDateExpedition() != null
                ? AvisExpedition.of(entity.getAvisDateExpedition(), entity.getAvisTransporteur(),
                        entity.getAvisNumeroSuivi(), entity.getAvisDateLivraisonEstimee())
                : null;

        return CommandeAchat.reconstruct(
                CommandeAchatId.of(entity.getId()),
                entity.getReference(),
                FournisseurId.of(entity.getFournisseurId()),
                EntrepotId.of(entity.getEntrepotDestinationId()),
                entity.getStatut(),
                lignes,
                entity.getDateAccuseReceptionFournisseur(),
                entity.getDelaiLivraisonConfirmeJours(),
                entity.getDateLivraisonConfirmee(),
                avis,
                entity.getMotifRejet(),
                entity.getCommentaire(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public CommandeAchatJpaEntity toEntity(CommandeAchat commande) {
        AvisExpedition avis = commande.getAvisExpedition();

        CommandeAchatJpaEntity entity = new CommandeAchatJpaEntity(
                commande.getId().getValue(),
                commande.getReference(),
                commande.getFournisseurId().getValue(),
                commande.getEntrepotDestinationId().getValue(),
                commande.getStatut(),
                commande.getDateAccuseReceptionFournisseur(),
                commande.getDelaiLivraisonConfirmeJours(),
                commande.getDateLivraisonConfirmee(),
                avis != null ? avis.getDateExpedition() : null,
                avis != null ? avis.getTransporteur() : null,
                avis != null ? avis.getNumeroSuivi() : null,
                avis != null ? avis.getDateLivraisonEstimee() : null,
                commande.getMotifRejet(),
                commande.getCommentaire());
        entity.setCreatedAt(commande.getCreatedAt());
        entity.setUpdatedAt(commande.getUpdatedAt());
        return entity;
    }

    public List<LigneCommandeAchatJpaEntity> toEntityLignes(CommandeAchat commande) {
        return commande.getLignes().stream()
                .map(ligne -> new LigneCommandeAchatJpaEntity(
                        ligne.getId().getValue(),
                        commande.getId().getValue(),
                        ligne.getMedicamentId().getValue(),
                        ligne.getConditionnementId().getValue(),
                        ligne.getQuantiteCommandee(),
                        ligne.getPrixUnitaire(),
                        ligne.getNumeroLot(),
                        ligne.getDateFabrication(),
                        ligne.getDateExpiration(),
                        ligne.getCertificatAnalyseUrl(),
                        ligne.getQuantiteExpediee(),
                        ligne.getQuantiteRecue(),
                        ligne.getQuantiteRefusee(),
                        ligne.getMotifRefus()))
                .toList();
    }

    private LigneCommandeAchat toDomainLigne(LigneCommandeAchatJpaEntity entity) {
        return LigneCommandeAchat.reconstruct(
                LigneCommandeAchatId.of(entity.getId()),
                MedicamentId.of(entity.getMedicamentId()),
                ConditionnementId.of(entity.getConditionnementId()),
                entity.getQuantiteCommandee(),
                entity.getPrixUnitaire(),
                entity.getNumeroLot(),
                entity.getDateFabrication(),
                entity.getDateExpiration(),
                entity.getCertificatAnalyseUrl(),
                entity.getQuantiteExpediee(),
                entity.getQuantiteRecue(),
                entity.getQuantiteRefusee(),
                entity.getMotifRefus());
    }
}
