package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class OpportuniteCarriereIntrouvableException extends NotFoundException {
    public OpportuniteCarriereIntrouvableException() {
        super("Opportunité de carrière introuvable", "OPPORTUNITE_CARRIERE_NOT_FOUND");
    }
}
