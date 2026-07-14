package ministere.sante.senpna.appeloffre.application.service;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.LigneOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;

import org.springframework.stereotype.Component;

@Component
public class OffreDetailAssembler {

    public OffreDetail assembler(OffreFournisseur offre) {
        return new OffreDetail(
                offre.getId().getValue(),
                offre.getAppelOffreId().getValue(),
                offre.getFournisseurId().getValue(),
                offre.getCommentaire(),
                offre.getStatut(),
                offre.getLignes().stream().map(this::assemblerLigne).toList(),
                offre.getCreatedAt(),
                offre.getUpdatedAt());
    }

    private LigneOffreDetail assemblerLigne(LigneOffre ligne) {
        return new LigneOffreDetail(
                ligne.getId().getValue(),
                ligne.getLigneAppelOffreId().getValue(),
                ligne.getPrixUnitaire(),
                ligne.getDelaiLivraisonJours());
    }
}
