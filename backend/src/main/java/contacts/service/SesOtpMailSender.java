package contacts.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@ConditionalOnProperty(name = "app.otp.dev-mode", havingValue = "false")
public class SesOtpMailSender implements OtpMailSender {

    private final SesClient sesClient;
    private final String fromEmail;

    public SesOtpMailSender(@Value("${aws.region}") String region, @Value("${app.mail.from}") String fromEmail) {
        this.sesClient = SesClient.builder().region(Region.of(region)).build();
        this.fromEmail = fromEmail;
    }

    @Override
    public void send(String toEmail, String otp) {
        SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data("Your verification code").build())
                        .body(Body.builder()
                                .text(Content.builder()
                                        .data("Your one-time verification code is: " + otp
                                                + "\nThis code expires in a few minutes.")
                                        .build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }
}
