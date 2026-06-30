package ministere.sante.senpna.auth.domain.port.out;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

public interface OtpSenderPort {

    void send(OtpChannel channel, String destination, String code);

}
