package contacts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Body for creating or replacing one email template. The id travels in the path, not here. */
@Data
public class EmailTemplateRequest {

    @NotBlank(message = "Heading is required")
    @Size(max = 200, message = "Heading must be at most 200 characters")
    private String heading;

    @NotBlank(message = "Body is required")
    @Size(max = 5000, message = "Body must be at most 5000 characters")
    private String body;
}
