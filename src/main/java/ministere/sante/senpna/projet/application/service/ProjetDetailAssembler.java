package ministere.sante.senpna.projet.application.service;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.model.Projet;

import org.springframework.stereotype.Component;

@Component
public class ProjetDetailAssembler {

    public ProjetDetail assembler(Projet projet) {
        return new ProjetDetail(
                projet.getId().getValue(),
                projet.getCategorie().name(),
                projet.getNom(),
                projet.getDescription(),
                projet.getObjectifs(),
                projet.getImpacts(),
                projet.getImageUrl(),
                projet.getStatut().name(),
                projet.getCreatedAt(),
                projet.getUpdatedAt());
    }
}
