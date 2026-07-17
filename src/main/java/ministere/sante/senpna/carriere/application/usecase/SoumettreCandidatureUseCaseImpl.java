package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.CandidatureCommandMapper;
import ministere.sante.senpna.carriere.application.service.CandidatureDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.SoumettreCandidatureCommand;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteFermeeException;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.SoumettreCandidatureUseCase;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureRepositoryPort;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.EventPublisherPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoumettreCandidatureUseCaseImpl implements SoumettreCandidatureUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final CandidatureRepositoryPort candidatureRepositoryPort;
    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final CandidatureCommandMapper commandMapper;
    private final CandidatureDetailAssembler assembler;
    private final EventPublisherPort eventPublisherPort;

    public SoumettreCandidatureUseCaseImpl(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            CandidatureRepositoryPort candidatureRepositoryPort,
            UserManagementRepositoryPort userManagementRepositoryPort, CandidatureCommandMapper commandMapper,
            CandidatureDetailAssembler assembler, EventPublisherPort eventPublisherPort) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.candidatureRepositoryPort = candidatureRepositoryPort;
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public CandidatureDetail soumettre(SoumettreCandidatureCommand command) {
        OpportuniteCarriere opportunite = opportuniteCarriereRepositoryPort
                .findById(OpportuniteCarriereId.of(command.opportuniteId()))
                .orElseThrow(OpportuniteCarriereIntrouvableException::new);

        if (!opportunite.accepteCandidatures()) {
            throw new OpportuniteFermeeException();
        }

        Civilite civilite = commandMapper.versCivilite(command.civilite());
        Email email = Email.of(command.email());
        Phone telephone = Phone.of(command.telephone());

        String emailContactRH = resoudreEmailContactRH(opportunite);

        Candidature candidature = Candidature.soumettre(new Candidature.SoumissionCommand(
                opportunite.getId().getValue(), civilite, command.nomComplet(), email, telephone,
                command.cvUrl(), command.lettreMotivationUrl(), command.messageComplementaire(),
                command.consentementRgpd(), opportunite.getTitre(), opportunite.getNomEntreprise(),
                emailContactRH));

        Candidature saved = candidatureRepositoryPort.save(candidature);

        // Publié depuis `candidature` (qui porte l'event accumulé par
        // `soumettre()`), pas depuis `saved` — l'adaptateur de persistance
        // reconstruit une nouvelle instance sans les events.
        eventPublisherPort.publishAndClear(candidature);

        return assembler.assembler(saved);
    }

    private String resoudreEmailContactRH(OpportuniteCarriere opportunite) {
        if (opportunite.getEmailContact() != null) {
            return opportunite.getEmailContact();
        }
        User auteur = userManagementRepositoryPort.findById(UserId.of(opportunite.getAuteurId()))
                .orElseThrow(UserNotFoundException::new);
        return auteur.getEmail().value();
    }
}
