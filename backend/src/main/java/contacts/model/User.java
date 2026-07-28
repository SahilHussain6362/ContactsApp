package contacts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private AuthProvider provider;

    // Contact ids this user has bookmarked. Kept on the user rather than the contact because
    // contacts are shared globally — one contact can be bookmarked by many users independently.
    // A LinkedHashSet preserves the order they were added in and makes re-bookmarking a no-op.
    private Set<String> bookmarkedContactIds = new LinkedHashSet<>();

    // The user's own message templates, used to prefill an outgoing mail or WhatsApp chat from a
    // contact. Lists rather than sets: order is the order the user created them in, which is the
    // order they are offered to pick from. Both are capped — see TemplateService.MAX_PER_TYPE.
    private List<EmailTemplate> emailTemplates = new ArrayList<>();
    private List<WhatsappTemplate> whatsappTemplates = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    /** Never null, even for user documents written before bookmarks existed. */
    public Set<String> getBookmarkedContactIds() {
        if (bookmarkedContactIds == null) {
            bookmarkedContactIds = new LinkedHashSet<>();
        }
        return bookmarkedContactIds;
    }

    /** Never null, even for user documents written before templates existed. */
    public List<EmailTemplate> getEmailTemplates() {
        if (emailTemplates == null) {
            emailTemplates = new ArrayList<>();
        }
        return emailTemplates;
    }

    /** Never null, even for user documents written before templates existed. */
    public List<WhatsappTemplate> getWhatsappTemplates() {
        if (whatsappTemplates == null) {
            whatsappTemplates = new ArrayList<>();
        }
        return whatsappTemplates;
    }
}
