package ministere.sante.senpna.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Émis par {@code StructureSanitaire.validerAdhesion()} (module
 * {@code organisation}) une fois la demande d'adhésion validée.
 *
 * <p>
 * Placé dans {@code shared} plutôt que dans {@code organisation} : c'est
 * un contrat d'intégration entre features (ici consommé par
 * {@code utilisateurs}, qui crée automatiquement le compte
 * {@code GESTIONNAIRE_STRUCTURE} correspondant), pas un détail interne de
 * l'agrégat qui l'émet. Ne transporte que des données primitives —
 * jamais l'agrégat {@code StructureSanitaire} lui-même.
 * </p>
 */
public record AdhesionValideeEvent(
        UUID structureSanitaireId,
        String structureNom,
        String responsableNom,
        String responsablePrenom,
        String email,
        Instant occurredAt) implements DomainEvent {

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
