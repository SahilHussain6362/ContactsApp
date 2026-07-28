package contacts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body for creating or replacing one WhatsApp template. Only a message — WhatsApp has no subject
 * line, so there is nothing for a heading to map onto.
 */
@Data
public class WhatsappTemplateRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message must be at most 5000 characters")
    private String message;
}
