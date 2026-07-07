package ministere.sante.senpna.carriere.domain.events;

import ministere.sante.senpna.shared.domain.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Émis par {@code Candidature.soumettre()} une fois une candidature créée.
 *
 * <p>
 * Consommé par {@code NouvelleCandidatureListener} (module {@code carriere})
 * pour déclencher, après commit et de façon asynchrone/best-effort, l'envoi
 * de l'accusé de réception au candidat et de la notification au contact RH
 * de l'offre. Ne transporte que des données primitives — jamais l'agrégat
 * {@code Candidature} lui-même — conformément à la convention déjà en place
 * pour {@code AdhesionValideeEvent} (module {@code shared}).
 * </p>
 *
 * @param emailContactRH e-mail de notification RH — l'e-mail de contact
 *                       explicite de l'offre s'il est renseigné, sinon
 *                       l'e-mail du compte auteur de l'offre (résolu par le
 *                       use case avant publication de l'event)
 */
public record NouvelleCandidatureEvent(
        UUID candidatureId,
        UUID opportuniteId,
        String titreOffre,
        String nomEntreprise,
        String nomCandidat,
        String emailCandidat,
        String telephoneCandidat,
        String emailContactRH,
        Instant occurredAt) implements DomainEvent {

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
