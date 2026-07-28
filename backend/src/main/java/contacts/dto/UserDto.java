package contacts.dto;

import contacts.model.AuthProvider;
import contacts.model.EmailTemplate;
import contacts.model.User;
import contacts.model.WhatsappTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;
    private String name;
    private String email;
    private AuthProvider provider;
    private Instant createdAt;

    // Sent as a list rather than a set so the JSON is a stable, ordered array for the client
    // to diff against its local mirror.
    private List<String> bookmarkedContactIds;

    // Templates ride along with the profile rather than having a read endpoint of their own: the
    // client already refreshes /auth/me on every sync, so they arrive with no extra round trip.
    private List<EmailTemplate> emailTemplates;
    private List<WhatsappTemplate> whatsappTemplates;

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.getCreatedAt(),
                new ArrayList<>(user.getBookmarkedContactIds()),
                new ArrayList<>(user.getEmailTemplates()),
                new ArrayList<>(user.getWhatsappTemplates())
        );
    }
}
