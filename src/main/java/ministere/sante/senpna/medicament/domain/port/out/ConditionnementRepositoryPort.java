package ministere.sante.senpna.medicament.domain.port.out;

import ministere.sante.senpna.medicament.domain.criteria.ConditionnementSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface ConditionnementRepositoryPort {

    Optional<Conditionnement> findById(ConditionnementId id);

    boolean existsByMedicamentIdAndNiveau(MedicamentId medicamentId, int niveau);

    boolean existsByMedicamentIdAndNiveauAndIdNot(MedicamentId medicamentId, int niveau, ConditionnementId id);

    boolean existsByMedicamentIdAndNomIgnoreCase(MedicamentId medicamentId, String nom);

    boolean existsByMedicamentIdAndNomIgnoreCaseAndIdNot(MedicamentId medicamentId, String nom,
            ConditionnementId id);

    /**
     * @return {@code true} si ce médicament possède déjà un conditionnement
     *         marqué {@code estUniteBase = true}.
     */
    boolean existsUniteBaseByMedicamentId(MedicamentId medicamentId);

    boolean existsUniteBaseByMedicamentIdAndIdNot(MedicamentId medicamentId, ConditionnementId id);

    /**
     * @return {@code true} si le conditionnement d'unité de base identifié
     *         est le <strong>seul</strong> conditionnement actif de ce
     *         médicament ayant {@code estUniteBase = true} — utilisé pour
     *         empêcher son archivage (règle : un médicament actif doit
     *         toujours conserver une unité de base).
     */
    boolean estUniqueUniteBaseActive(MedicamentId medicamentId, ConditionnementId id);

    Conditionnement save(Conditionnement conditionnement);

    PageResult<Conditionnement> search(ConditionnementSearchCriteria criteria, PageRequest pageRequest);
}
