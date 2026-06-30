package ministere.sante.senpna.shared.infrastructure;

import ministere.sante.senpna.shared.domain.port.out.UuidGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RandomUuidGeneratorAdapter implements UuidGeneratorPort {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
