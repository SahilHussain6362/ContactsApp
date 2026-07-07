package contacts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "email_otps")
public class EmailOtp {

    @Id
    private String id;

    @Indexed
    private String email;

    private OtpPurpose purpose;

    private String otpHash;

    // TTL index: Mongo automatically deletes the document once this instant is in the past.
    // Acts as a backstop alongside the explicit deletes in AuthService.
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    private int attemptCount;

    private Instant createdAt;
}
