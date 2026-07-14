package ministere.sante.senpna.appeloffre.application.service;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreSummary;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.LigneAppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;

import org.springframework.stereotype.Component;

@Component
public class AppelOffreDetailAssembler {

    public AppelOffreDetail assembler(AppelOffre appelOffre) {
        return new AppelOffreDetail(
                appelOffre.getId().getValue(),
                appelOffre.getReference(),
                appelOffre.getObjet(),
                appelOffre.getDateCloture(),
                appelOffre.getStatut(),
                appelOffre.getLignes().stream().map(this::assemblerLigne).toList(),
                appelOffre.getCreatedAt(),
                appelOffre.getUpdatedAt());
    }

    public AppelOffreSummary assemblerResume(AppelOffre appelOffre) {
        return new AppelOffreSummary(
                appelOffre.getId().getValue(),
                appelOffre.getReference(),
                appelOffre.getObjet(),
                appelOffre.getDateCloture(),
                appelOffre.getStatut(),
                appelOffre.getLignes().size(),
                appelOffre.getCreatedAt());
    }

    private LigneAppelOffreDetail assemblerLigne(LigneAppelOffre ligne) {
        return new LigneAppelOffreDetail(
                ligne.getId().getValue(),
                ligne.getMedicamentId().getValue(),
                ligne.getDesignation(),
                ligne.getQuantiteEstimee(),
                ligne.getUniteBase());
    }
}
