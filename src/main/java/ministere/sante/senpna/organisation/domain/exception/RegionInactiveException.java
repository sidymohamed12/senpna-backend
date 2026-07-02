package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

public class RegionInactiveException extends BusinessRuleException {
    public RegionInactiveException() {
        super("La région est désactivée et ne peut plus recevoir de nouveaux rattachements", "REGION_INACTIVE");
    }
}
