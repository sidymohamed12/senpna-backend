package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.CandidatureDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.GetCandidatureQuery;
import ministere.sante.senpna.carriere.domain.exception.CandidatureIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.port.in.GetCandidatureUseCase;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.CandidatureId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCandidatureUseCaseImpl implements GetCandidatureUseCase {

    private final CandidatureRepositoryPort candidatureRepositoryPort;
    private final CandidatureDetailAssembler assembler;

    public GetCandidatureUseCaseImpl(CandidatureRepositoryPort candidatureRepositoryPort,
            CandidatureDetailAssembler assembler) {
        this.candidatureRepositoryPort = candidatureRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatureDetail obtenir(GetCandidatureQuery query) {
        Candidature candidature = candidatureRepositoryPort.findById(CandidatureId.of(query.candidatureId()))
                .orElseThrow(CandidatureIntrouvableException::new);
        return assembler.assembler(candidature);
    }
}
