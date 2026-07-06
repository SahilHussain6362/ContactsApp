package contacts.controller;

import contacts.dto.BatchSyncRequest;
import contacts.dto.BatchSyncResponse;
import contacts.dto.ContactRequest;
import contacts.model.Contact;
import contacts.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @GetMapping
    public List<Contact> getAllContacts() {
        return service.getAllContacts();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contact create(@RequestBody @Valid ContactRequest req) {
        return service.create(req);
    }


    @PutMapping("/{id}")
    public Contact update(@PathVariable String id, @RequestBody @Valid ContactRequest req) {
        return service.update(id, req);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.softDelete(id);
    }


    // Returns all records (including soft-deleted) updated after `since` epoch millis.
    // Android client calls this on every sync to get incremental changes.
    @GetMapping("/changes")
    public List<Contact> getChanges(@RequestParam long since) {
        return service.getChangesSince(since);
    }


    // Accepts a batch of offline pending changes and returns created/updated contacts
    // plus IDs of deleted contacts. Client stores serverTimestamp as next lastSync value.
    @PostMapping("/batch-sync")
    public BatchSyncResponse batchSync(@RequestBody BatchSyncRequest req) {
        return service.batchSync(req);
    }
}
