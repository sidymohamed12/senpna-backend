package ministere.sante.senpna.shared.domain.events;

import java.time.Instant;

public interface DomainEvent {

    Instant getOccurredAt();

    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
