package contacts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "hr_contacts")
public class HrContact {

    @Id
    private String id;

    private String name;
    private String company;
    private String mobile;
    private List<String> emails;
    private String linkedinProfile;

    private Instant createdAt;
    private Instant updatedAt;

    // Soft-delete flag: contacts are never physically removed.
    // Incremental sync sends deleted=true records to clients so they purge locally.
    private boolean deleted = false;
}
