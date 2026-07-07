package ministere.sante.senpna.carriere.application.service;

import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.model.Candidature;

import org.springframework.stereotype.Component;

@Component
public class CandidatureDetailAssembler {

    public CandidatureDetail assembler(Candidature candidature) {
        return new CandidatureDetail(
                candidature.getId().getValue(),
                candidature.getOpportuniteId(),
                candidature.getCivilite().name(),
                candidature.getNomComplet(),
                candidature.getEmail().value(),
                candidature.getTelephone().value(),
                candidature.getCvUrl(),
                candidature.getLettreMotivationUrl(),
                candidature.getMessageComplementaire(),
                candidature.isConsentementRgpd(),
                candidature.getDateCandidature());
    }
}
