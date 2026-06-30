package ministere.sante.senpna.shared.infrastructure.event;

import ministere.sante.senpna.shared.domain.events.DomainEvent;
import ministere.sante.senpna.shared.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class SpringDomainEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        Objects.requireNonNull(event, "L'event ne peut pas être null");
        log.debug("[DomainEvent] Publication de '{}' à {}", event.eventType(), event.getOccurredAt());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        Objects.requireNonNull(events, "La liste d'events ne peut pas être null");
        if (events.isEmpty()) {
            return;
        }
        log.debug("[DomainEvent] Publication de {} event(s)", events.size());
        events.forEach(this::publish);
    }
}
