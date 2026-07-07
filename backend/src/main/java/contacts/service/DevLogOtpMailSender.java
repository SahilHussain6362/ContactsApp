package contacts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

// Active by default until real SES credentials are configured — logs the OTP instead of emailing it
// so the full auth flow is testable without AWS set up. Flip app.otp.dev-mode=false to use SES instead.
@Slf4j
@Service
@ConditionalOnProperty(name = "app.otp.dev-mode", havingValue = "true", matchIfMissing = true)
public class DevLogOtpMailSender implements OtpMailSender {

    @Override
    public void send(String toEmail, String otp) {
        log.warn("[DEV-MODE] OTP for {} is {}", toEmail, otp);
    }
}
