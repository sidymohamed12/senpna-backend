package ministere.sante.senpna.shared.domain.port.out;


public interface TokenRevocationPort {

    boolean estInvalide(String token);
}
