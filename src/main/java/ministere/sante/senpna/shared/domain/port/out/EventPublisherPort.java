package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.events.DomainEvent;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.util.List;

public interface EventPublisherPort {

    void publish(DomainEvent event);

    void publishAll(List<DomainEvent> events);

    default void publishAndClear(AggregateRoot<?> aggregate) {
        List<DomainEvent> events = aggregate.getDomainEvents();

        if (!events.isEmpty()) {
            publishAll(events);
            aggregate.clearDomainEvents();
        }
    }
}
