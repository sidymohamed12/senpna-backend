package ministere.sante.senpna.stock.domain.exception.lot;

import ministere.sante.senpna.shared.domain.exception.ForbiddenException;

/**
 * Levée lorsqu'un acteur PRA tente de consulter ou gérer (bloquer,
 * débloquer, modifier le prix) un lot qui n'a jamais transité par son
 * entrepôt — c'est-à-dire pour lequel aucune ligne de stock n'existe
 * dans son entrepôt. Ne s'applique pas aux acteurs PNA (portée nationale
 * sur le catalogue des lots).
 */
public class LotHorsPorteeException extends ForbiddenException {
    public LotHorsPorteeException() {
        super("Ce lot n'est pas présent dans le stock de votre entrepôt", "LOT_OUT_OF_SCOPE");
    }
}
