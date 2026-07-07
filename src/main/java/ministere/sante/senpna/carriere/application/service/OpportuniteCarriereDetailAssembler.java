package ministere.sante.senpna.carriere.application.service;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;

import org.springframework.stereotype.Component;

@Component
public class OpportuniteCarriereDetailAssembler {

    public OpportuniteCarriereDetail assembler(OpportuniteCarriere opportunite) {
        return new OpportuniteCarriereDetail(
                opportunite.getId().getValue(),
                opportunite.getTitre(),
                opportunite.getNomEntreprise(),
                opportunite.getDescription(),
                opportunite.getFicheDePosteUrl(),
                opportunite.getLieu(),
                opportunite.getTypeContrat().name(),
                opportunite.getDateDebut(),
                opportunite.getDateLimiteCandidature(),
                opportunite.getAuteurId(),
                opportunite.getAuteurNom(),
                opportunite.getEmailContact(),
                opportunite.getStatutEffectif().name(),
                opportunite.getCreatedAt(),
                opportunite.getUpdatedAt());
    }
}
