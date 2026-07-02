package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class RegionIntrouvableException extends NotFoundException {
    public RegionIntrouvableException() {
        super("Région introuvable", "REGION_NOT_FOUND");
    }
}
