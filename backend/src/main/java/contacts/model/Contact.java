package contacts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contact")
public class Contact {

    @Id
    private String id;

    private String name;
    private String company;
    private String mobile;
    private List<String> emails;
    private String linkedinProfile;

    // Server-owned: never set from a client request. Only a backend process or a direct
    // DB write flips this, so a contact cannot be passed off as verified by whoever added it.
    private boolean verified = false;

    // When true the contact is visible only to its creator. Absent on documents that predate
    // the flag, which Mongo reads back as false — those stay shared, matching prior behaviour.
    // @JsonProperty is required: Jackson would otherwise derive "private" from isPrivate().
    @JsonProperty("isPrivate")
    private boolean isPrivate = false;

    // Id of the user who first created this contact. Stays with the original creator even
    // when a later create merges into this record as a duplicate — the merging user did not
    // author it. Null for contacts that predate ownership tracking, so those belong to nobody.
    @Indexed
    private String createdBy;

    private Instant createdAt;
    private Instant updatedAt;

    // Soft-delete flag: contacts are never physically removed.
    // Incremental sync sends deleted=true records to clients so they purge locally.
    private boolean deleted = false;
}
