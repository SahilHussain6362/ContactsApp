package contacts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A saved subject + body the user can prefill an outgoing mail with.
 *
 * Embedded in {@link User} rather than stored in its own collection: templates are the user's own
 * wording, are only ever read alongside the profile, and are capped at three — so there is nothing
 * to gain from a separate document. The id is generated server-side so the client can address a
 * single template for edit and delete without positional indexes it might race against.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    private String id;
    private String heading;
    private String body;
}
