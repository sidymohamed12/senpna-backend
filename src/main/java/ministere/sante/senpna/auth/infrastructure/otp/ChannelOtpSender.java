package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

public interface ChannelOtpSender {

    OtpChannel channel();

    void send(String destination, String code);
}
