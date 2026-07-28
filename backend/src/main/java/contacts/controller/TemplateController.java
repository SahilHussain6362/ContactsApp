package contacts.controller;

import contacts.dto.EmailTemplateRequest;
import contacts.dto.UserDto;
import contacts.dto.WhatsappTemplateRequest;
import contacts.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * The caller's own message templates. Split by type rather than taking a discriminator field,
 * because the two shapes differ: an email template carries a heading and a body, a WhatsApp one
 * only a message.
 *
 * There is no GET here — templates come down with the profile on /api/v1/auth/me, which the client
 * already refreshes on every sync. Every write returns the updated profile for the same reason.
 */
@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping("/email")
    public UserDto createEmailTemplate(@RequestBody @Valid EmailTemplateRequest req) {
        return templateService.addEmailTemplate(req.getHeading(), req.getBody());
    }

    @PutMapping("/email/{id}")
    public UserDto updateEmailTemplate(@PathVariable String id,
                                       @RequestBody @Valid EmailTemplateRequest req) {
        return templateService.updateEmailTemplate(id, req.getHeading(), req.getBody());
    }

    @DeleteMapping("/email/{id}")
    public UserDto deleteEmailTemplate(@PathVariable String id) {
        return templateService.deleteEmailTemplate(id);
    }

    @PostMapping("/whatsapp")
    public UserDto createWhatsappTemplate(@RequestBody @Valid WhatsappTemplateRequest req) {
        return templateService.addWhatsappTemplate(req.getMessage());
    }

    @PutMapping("/whatsapp/{id}")
    public UserDto updateWhatsappTemplate(@PathVariable String id,
                                          @RequestBody @Valid WhatsappTemplateRequest req) {
        return templateService.updateWhatsappTemplate(id, req.getMessage());
    }

    @DeleteMapping("/whatsapp/{id}")
    public UserDto deleteWhatsappTemplate(@PathVariable String id) {
        return templateService.deleteWhatsappTemplate(id);
    }
}
