package ministere.sante.senpna.stock.domain.port.out;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.AlertePeremptionCriteria;
import ministere.sante.senpna.stock.domain.criteria.LotSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import java.util.List;
import java.util.Optional;

public interface LotRepositoryPort {

    Optional<Lot> findById(LotId id);

    boolean existsByMedicamentIdAndNumeroLotIgnoreCase(MedicamentId medicamentId, String numeroLot);

    Lot save(Lot lot);

    PageResult<Lot> search(LotSearchCriteria criteria, PageRequest pageRequest);

    /**
     * Lots {@code ACTIF} du médicament donné (si précisé), non expirés,
     * triés par date d'expiration croissante — application directe de la
     * règle FEFO (cf. doc. métier §8).
     */
    List<Lot> findActifsNonExpiresParMedicamentTriesFefo(MedicamentId medicamentId);

    PageResult<Lot> findExpirantAvant(AlertePeremptionCriteria criteria, PageRequest pageRequest);

    /** Lots {@code ACTIF} dont la date d'expiration est dépassée — utilisé par le job d'expiration automatique. */
    List<Lot> findActifsExpires();
}
