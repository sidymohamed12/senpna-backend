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

        return CommandeAchat.builder()
            .id(CommandeAchatId.of(entity.getId()))
            .reference(entity.getReference())
            .fournisseurId(FournisseurId.of(entity.getFournisseurId()))
            .entrepotDestinationId(EntrepotId.of(entity.getEntrepotDestinationId()))
            .statut(entity.getStatut())
            .lignes(lignes)
            .dateAccuseReceptionFournisseur(entity.getDateAccuseReceptionFournisseur())
            .delaiLivraisonConfirmeJours(entity.getDelaiLivraisonConfirmeJours())
            .dateLivraisonConfirmee(entity.getDateLivraisonConfirmee())
            .avisExpedition(avis)
            .motifRejet(entity.getMotifRejet())
            .commentaire(entity.getCommentaire())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public CommandeAchatJpaEntity toEntity(CommandeAchat commande) {
        AvisExpedition avis = commande.getAvisExpedition();

        CommandeAchatJpaEntity entity = CommandeAchatJpaEntity.builder()
            .id(commande.getId().getValue())
            .reference(commande.getReference())
            .fournisseurId(commande.getFournisseurId().getValue())
            .entrepotDestinationId(commande.getEntrepotDestinationId().getValue())
            .statut(commande.getStatut())
            .dateAccuseReceptionFournisseur(commande.getDateAccuseReceptionFournisseur())
            .delaiLivraisonConfirmeJours(commande.getDelaiLivraisonConfirmeJours())
            .dateLivraisonConfirmee(commande.getDateLivraisonConfirmee())
            .avisDateExpedition(avis != null ? avis.getDateExpedition() : null)
            .avisTransporteur(avis != null ? avis.getTransporteur() : null)
            .avisNumeroSuivi(avis != null ? avis.getNumeroSuivi() : null)
            .avisDateLivraisonEstimee(avis != null ? avis.getDateLivraisonEstimee() : null)
            .motifRejet(commande.getMotifRejet())
            .commentaire(commande.getCommentaire())
            .build();
        entity.setCreatedAt(commande.getCreatedAt());
        entity.setUpdatedAt(commande.getUpdatedAt());
        return entity;
    }

    public List<LigneCommandeAchatJpaEntity> toEntityLignes(CommandeAchat commande) {
        return commande.getLignes().stream()
                .map(ligne -> LigneCommandeAchatJpaEntity.builder()
                    .id(ligne.getId().getValue())
                    .commandeAchatId(commande.getId().getValue())
                    .medicamentId(ligne.getMedicamentId().getValue())
                    .conditionnementId(ligne.getConditionnementId().getValue())
                    .quantiteCommandee(ligne.getQuantiteCommandee())
                    .prixUnitaire(ligne.getPrixUnitaire())
                    .numeroLot(ligne.getNumeroLot())
                    .dateFabrication(ligne.getDateFabrication())
                    .dateExpiration(ligne.getDateExpiration())
                    .certificatAnalyseUrl(ligne.getCertificatAnalyseUrl())
                    .quantiteExpediee(ligne.getQuantiteExpediee())
                    .quantiteRecue(ligne.getQuantiteRecue())
                    .quantiteRefusee(ligne.getQuantiteRefusee())
                    .motifRefus(ligne.getMotifRefus())
                    .build())
                .toList();
    }

    private LigneCommandeAchat toDomainLigne(LigneCommandeAchatJpaEntity entity) {
        return LigneCommandeAchat.builder()
            .id(LigneCommandeAchatId.of(entity.getId()))
            .medicamentId(MedicamentId.of(entity.getMedicamentId()))
            .conditionnementId(ConditionnementId.of(entity.getConditionnementId()))
            .quantiteCommandee(entity.getQuantiteCommandee())
            .prixUnitaire(entity.getPrixUnitaire())
            .numeroLot(entity.getNumeroLot())
            .dateFabrication(entity.getDateFabrication())
            .dateExpiration(entity.getDateExpiration())
            .certificatAnalyseUrl(entity.getCertificatAnalyseUrl())
            .quantiteExpediee(entity.getQuantiteExpediee())
            .quantiteRecue(entity.getQuantiteRecue())
            .quantiteRefusee(entity.getQuantiteRefusee())
            .motifRefus(entity.getMotifRefus())
            .build();
    }
}
