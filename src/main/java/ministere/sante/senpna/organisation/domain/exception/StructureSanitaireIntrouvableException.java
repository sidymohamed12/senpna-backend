package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class StructureSanitaireIntrouvableException extends NotFoundException {
    public StructureSanitaireIntrouvableException() {
        super("Structure sanitaire introuvable", "STRUCTURE_SANITAIRE_NOT_FOUND");
    }
}
