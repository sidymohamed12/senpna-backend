package ministere.sante.senpna.carriere.domain.port.out;

import ministere.sante.senpna.carriere.domain.criteria.OpportuniteCarriereSearchCriteria;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OpportuniteCarriereRepositoryPort {

    Optional<OpportuniteCarriere> findById(OpportuniteCarriereId id);

    OpportuniteCarriere save(OpportuniteCarriere opportuniteCarriere);

    PageResult<OpportuniteCarriere> search(OpportuniteCarriereSearchCriteria criteria, PageRequest pageRequest);

    /**
     * Offres {@code OUVERT}/{@code EN_COURS} dont la date limite de
     * candidature est strictement antérieure à {@code reference} —
     * utilisé par le job planifié de clôture automatique.
     */
    List<OpportuniteCarriere> findOuvertesExpirees(LocalDate reference);
}
