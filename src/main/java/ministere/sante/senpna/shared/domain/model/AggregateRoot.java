package ministere.sante.senpna.shared.domain.model;

import ministere.sante.senpna.shared.domain.events.DomainEvent;
import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Racine d'agrégat — étend {@link BaseEntity} avec la gestion des Domain
 * Events.
 *
 * <p>
 * Les Domain Events sont accumulés dans l'agrégat pendant l'exécution
 * des commandes métier, puis publiés par le use case après persistance.
 * </p>
 *
 * <p>
 * La liste des events est défensive : {@link #getDomainEvents()} retourne
 * une vue non modifiable.
 * </p>
 */
public abstract class AggregateRoot<I extends EntityId> extends BaseEntity<I> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(I id) {
        super(id);
    }

    protected AggregateRoot(I id, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
    }

    protected void registerEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
