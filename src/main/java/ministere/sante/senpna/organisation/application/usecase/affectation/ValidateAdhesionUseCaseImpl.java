package ministere.sante.senpna.organisation.application.usecase.affectation;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ValidateAdhesionCommand;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.affectation.ValidateAdhesionUseCase;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.shared.domain.port.out.EventPublisherPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valide la demande d'adhésion d'une structure sanitaire : elle devient
 * active et peut dès lors être rattachée à une région/PRA puis passer des
 * commandes (cf. {@code AssignStructureToRegionUseCase},
 * {@code AssignStructureToPraUseCase}).
 *
 * <p>
 * Publie {@code AdhesionValideeEvent} — consommé (après commit de la
 * transaction) par le module {@code utilisateurs} pour créer
 * automatiquement le compte {@code GESTIONNAIRE_STRUCTURE} du responsable
 * désigné et lui envoyer ses identifiants par e-mail.
 * </p>
 */
@Service
public class ValidateAdhesionUseCaseImpl implements ValidateAdhesionUseCase {

    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;

    public ValidateAdhesionUseCaseImpl(StructureSanitaireRepositoryPort structureSanitaireRepositoryPort,
            EventPublisherPort eventPublisherPort,
            StructureSanitaireDetailAssembler structureSanitaireDetailAssembler) {
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.structureSanitaireDetailAssembler = structureSanitaireDetailAssembler;
    }

    @Override
    @Transactional
    public StructureSanitaireDetail valider(ValidateAdhesionCommand command) {
        StructureSanitaire structure = structureSanitaireRepositoryPort
                .findById(StructureSanitaireId.of(command.structureId()))
                .orElseThrow(StructureSanitaireIntrouvableException::new);

        structure.validerAdhesion();

        StructureSanitaire saved = structureSanitaireRepositoryPort.save(structure);

        // Publié depuis `structure` (l'instance qui a accumulé l'event via
        // validerAdhesion()), pas depuis `saved` : l'adaptateur de
        // persistance reconstruit une nouvelle instance sans les events.
        eventPublisherPort.publishAndClear(structure);

        return structureSanitaireDetailAssembler.assembler(saved);
    }
}
