package contacts.dto;

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
    private boolean verified;

    @AssertTrue(message = "At least one of mobile or email must be provided")
    public boolean isContactInfoPresent() {
        boolean hasEmail = emails != null && emails.stream().anyMatch(e -> e != null && !e.isBlank());
        return (mobile != null && !mobile.isBlank()) || hasEmail;
    }
}
