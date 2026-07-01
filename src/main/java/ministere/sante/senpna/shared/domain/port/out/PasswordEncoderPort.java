package ministere.sante.senpna.shared.domain.port.out;

public interface PasswordEncoderPort {

    String encoder(String motDePasseBrut);

    boolean correspond(String motDePasseBrut, String hash);
}
