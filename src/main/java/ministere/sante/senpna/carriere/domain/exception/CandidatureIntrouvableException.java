package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class CandidatureIntrouvableException extends NotFoundException {
    public CandidatureIntrouvableException() {
        super("Candidature introuvable", "CANDIDATURE_NOT_FOUND");
    }
}
