package contacts.service;

public interface OtpMailSender {

    void send(String toEmail, String otp);
}
