package contacts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Only the display name is editable. Email and provider are owned by the identity provider —
 * they key the user lookup at login, so letting a client rewrite them would orphan the account.
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;
}
