package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class RegionInactiveException extends SenPnaException {
    public RegionInactiveException() {
        super("La région est désactivée et ne peut plus recevoir de nouveaux rattachements", "REGION_INACTIVE", ErrorCategory.BUSINESS_RULE);
    }
}
