package contacts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ContactRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Company is required")
    private String company;

    private String mobile;
    private List<String> emails;
    private String linkedinProfile;

    // No `verified` field on purpose: that flag is server-owned (see Contact.verified), so a
    // client sending it would be ignored — leaving it out makes the boundary explicit instead.

    // Only the contact's creator can change this; see ContactService. @JsonProperty is required
    // because Jackson derives the name "private" from the isPrivate() accessor Lombok generates.
    @JsonProperty("isPrivate")
    private boolean isPrivate;

    @AssertTrue(message = "At least one of mobile or email must be provided")
    public boolean isContactInfoPresent() {
        boolean hasEmail = emails != null && emails.stream().anyMatch(e -> e != null && !e.isBlank());
        return (mobile != null && !mobile.isBlank()) || hasEmail;
    }
}
