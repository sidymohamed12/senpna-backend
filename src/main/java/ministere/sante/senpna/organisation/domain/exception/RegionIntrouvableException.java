package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class RegionIntrouvableException extends SenPnaException {
    public RegionIntrouvableException() {
        super("Région introuvable", "REGION_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
