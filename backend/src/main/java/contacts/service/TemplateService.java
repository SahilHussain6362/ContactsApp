package contacts.service;

import contacts.dto.UserDto;
import contacts.model.EmailTemplate;
import contacts.model.User;
import contacts.model.WhatsappTemplate;
import contacts.repository.UserRepository;
import contacts.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Create, edit and delete the caller's own message templates.
 *
 * Every method returns the whole {@link UserDto} rather than the single template it touched, so the
 * client overwrites its cached session user wholesale — the same contract the bookmark routes use,
 * and the reason the app never has to reconcile a partially-updated template list.
 */
@Service
public class TemplateService {

    /**
     * How many templates one user may keep, per type. The client mirrors this number in its own
     * pre-flight warning; the checks here are what actually enforce it.
     */
    public static final int MAX_PER_TYPE = 3;

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public TemplateService(UserRepository userRepository, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    public UserDto addEmailTemplate(String heading, String body) {
        User user = currentUser.require();
        List<EmailTemplate> templates = user.getEmailTemplates();
        requireRoom(templates.size(), "email");
        String id = UUID.randomUUID().toString();
        templates.add(new EmailTemplate(id, heading.trim(), body.trim()));
        log.info("User {} added email template {}", user.getId(), id);
        return save(user);
    }

    public UserDto updateEmailTemplate(String id, String heading, String body) {
        User user = currentUser.require();
        EmailTemplate template = find(user.getEmailTemplates(), t -> id.equals(t.getId()), id);
        template.setHeading(heading.trim());
        template.setBody(body.trim());
        log.info("User {} updated email template {}", user.getId(), id);
        return save(user);
    }

    public UserDto deleteEmailTemplate(String id) {
        User user = currentUser.require();
        if (!user.getEmailTemplates().removeIf(t -> id.equals(t.getId()))) {
            throw notFound(id);
        }
        log.info("User {} deleted email template {}", user.getId(), id);
        return save(user);
    }

    public UserDto addWhatsappTemplate(String message) {
        User user = currentUser.require();
        List<WhatsappTemplate> templates = user.getWhatsappTemplates();
        requireRoom(templates.size(), "WhatsApp");
        String id = UUID.randomUUID().toString();
        templates.add(new WhatsappTemplate(id, message.trim()));
        log.info("User {} added WhatsApp template {}", user.getId(), id);
        return save(user);
    }

    public UserDto updateWhatsappTemplate(String id, String message) {
        User user = currentUser.require();
        WhatsappTemplate template = find(user.getWhatsappTemplates(), t -> id.equals(t.getId()), id);
        template.setMessage(message.trim());
        log.info("User {} updated WhatsApp template {}", user.getId(), id);
        return save(user);
    }

    public UserDto deleteWhatsappTemplate(String id) {
        User user = currentUser.require();
        if (!user.getWhatsappTemplates().removeIf(t -> id.equals(t.getId()))) {
            throw notFound(id);
        }
        log.info("User {} deleted WhatsApp template {}", user.getId(), id);
        return save(user);
    }

    /**
     * 409 rather than 400: the request itself is well-formed, it is the stored state that leaves no
     * room. The reason text is written to be shown to the user as-is — the Android client surfaces
     * a ResponseStatusException's reason directly in a snackbar.
     */
    private void requireRoom(int currentCount, String typeLabel) {
        if (currentCount >= MAX_PER_TYPE) {
            log.warn("User {} hit the {}-template limit ({})", currentUser.requireId(), typeLabel, MAX_PER_TYPE);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You can store at most "
                    + MAX_PER_TYPE + " " + typeLabel + " templates. Delete one to add another.");
        }
    }

    private <T> T find(List<T> templates, Predicate<T> matchesId, String id) {
        return templates.stream()
                .filter(matchesId)
                .findFirst()
                .orElseThrow(() -> notFound(id));
    }

    private ResponseStatusException notFound(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id);
    }

    private UserDto save(User user) {
        user.setUpdatedAt(Instant.now());
        return UserDto.from(userRepository.save(user));
    }
}
