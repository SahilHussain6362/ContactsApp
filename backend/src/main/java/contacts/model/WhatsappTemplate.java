package contacts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A saved message the user can prefill a WhatsApp chat with. Deliberately has no heading —
 * WhatsApp has no subject line, so the whole template is one body of text.
 *
 * Embedded in {@link User} for the same reasons as {@link EmailTemplate}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappTemplate {

    private String id;
    private String message;
}
