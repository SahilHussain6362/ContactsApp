package contacts.service;

import contacts.dto.BatchSyncRequest;
import contacts.dto.BatchSyncResponse;
import contacts.dto.ContactRequest;
import contacts.dto.SyncChange;
import contacts.dto.UserDto;
import contacts.model.Contact;
import contacts.model.User;
import contacts.repository.ContactRepository;
import contacts.repository.UserRepository;
import contacts.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class ContactService {

    private final ContactRepository repository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public ContactService(ContactRepository repository,
                          UserRepository userRepository,
                          CurrentUser currentUser) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    public List<Contact> getAllContacts() {
        return repository.findVisibleTo(currentUser.requireId());
    }

    /** Contacts the caller authored. The client normally filters its local mirror instead. */
    public List<Contact> getMyContacts() {
        return repository.findByCreatedByAndDeletedFalseOrderByCompanyAscNameAsc(currentUser.requireId());
    }

    /**
     * A private contact exists only for its creator; everyone else is told it is not there.
     * Contacts with no creator (they predate ownership tracking) can never be private, so they
     * stay readable by everybody.
     */
    private boolean isVisibleTo(Contact contact, String callerId) {
        return !contact.isPrivate() || Objects.equals(contact.getCreatedBy(), callerId);
    }

    /**
     * 404 rather than 403 for a private contact the caller does not own: a distinct "forbidden"
     * would confirm the contact exists, which is exactly what the flag is meant to hide.
     */
    private Contact requireVisible(String id) {
        Contact contact = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + id));
        if (!isVisibleTo(contact, currentUser.requireId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + id);
        }
        return contact;
    }

    public UserDto addBookmark(String contactId) {
        Contact contact = requireVisible(contactId);
        if (contact.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + contactId);
        }
        User user = currentUser.require();
        user.getBookmarkedContactIds().add(contactId);
        user.setUpdatedAt(Instant.now());
        return UserDto.from(userRepository.save(user));
    }

    /**
     * Unbookmarking does not check that the contact still exists — a client removing a bookmark
     * for a contact that has since been deleted server-side must still be able to clean up.
     */
    public UserDto removeBookmark(String contactId) {
        User user = currentUser.require();
        user.getBookmarkedContactIds().remove(contactId);
        user.setUpdatedAt(Instant.now());
        return UserDto.from(userRepository.save(user));
    }

    public List<Contact> getChangesSince(long epochMillis) {
        String callerId = currentUser.requireId();
        return repository.findByUpdatedAtAfter(Instant.ofEpochMilli(epochMillis)).stream()
                .map(contact -> isVisibleTo(contact, callerId) ? contact : tombstone(contact))
                .toList();
    }

    /**
     * Stand-in for a contact the caller may not read. Skipping it outright would strand a stale
     * copy on any client that synced it while it was still shared, so instead we send back the
     * shape the client already knows how to purge — an id flagged deleted — with every field the
     * caller is not entitled to see left empty. Never saved; this exists only on the wire.
     */
    private Contact tombstone(Contact contact) {
        Contact stripped = new Contact();
        stripped.setId(contact.getId());
        stripped.setName("");
        stripped.setCompany("");
        stripped.setEmails(List.of());
        stripped.setUpdatedAt(contact.getUpdatedAt());
        stripped.setDeleted(true);
        return stripped;
    }

    public Contact create(ContactRequest req) {
        String callerId = currentUser.requireId();

        // If a non-deleted contact already exists with the same email, mobile, or
        // LinkedIn profile, update that record instead of creating a duplicate.
        Contact existing = findDuplicate(req, callerId);
        if (existing != null) {
            if(Objects.isNull(existing.getMobile())) existing.setMobile(req.getMobile());

            Set<String> uniqueEmails = new LinkedHashSet<>(existing.getEmails());
            uniqueEmails.addAll(req.getEmails());
            existing.setEmails(new ArrayList<>(uniqueEmails));

            if(Objects.isNull(existing.getLinkedinProfile())) existing.setLinkedinProfile(req.getLinkedinProfile());

            // isPrivate is the owner's call alone. A merge by anyone else leaves it untouched,
            // otherwise re-adding a shared contact as private would hide it from everybody else.
            if (Objects.equals(existing.getCreatedBy(), callerId)) {
                existing.setPrivate(req.isPrivate());
            }

            existing.setUpdatedAt(Instant.now());
            return repository.save(existing);
        }

        Instant now = Instant.now();
        Contact contact = new Contact();
        contact.setName(req.getName());
        contact.setCompany(req.getCompany());
        contact.setMobile(req.getMobile());
        contact.setEmails(req.getEmails());
        contact.setLinkedinProfile(req.getLinkedinProfile());
        contact.setPrivate(req.isPrivate());
        // verified is intentionally not set from req — a new contact always starts unverified and
        // only a backend process or a direct DB write can promote it.
        // Only a genuinely new record gets an author. The duplicate-merge branch above leaves
        // createdBy alone so a contact keeps crediting whoever first added it.
        contact.setCreatedBy(callerId);
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);
        contact.setDeleted(false);
        return repository.save(contact);
    }

    // Returns the first existing non-deleted contact that shares any email, mobile,
    // or linkedinProfile with the request. Emails take priority, then mobile, then LinkedIn.
    // Candidates the caller cannot see are excluded by the queries themselves.
    private Contact findDuplicate(ContactRequest req, String callerId) {

        String mobile = req.getMobile();
        if (mobile != null && !mobile.isBlank()) {
            Contact match = firstMergeable(repository.findVisibleByMobile(callerId, mobile), req, callerId);
            if (match != null) return match;
        }

        String linkedin = req.getLinkedinProfile();
        if (linkedin != null && !linkedin.isBlank()) {
            Contact match = firstMergeable(repository.findVisibleByLinkedinProfile(callerId, linkedin), req, callerId);
            if (match != null) return match;
        }

        return null;
    }

    // A private create only ever merges into a record the caller already owns. Folding it into
    // someone else's shared contact would turn that contact private and take it away from every
    // other user, so a duplicate record is the lesser evil there.
    private Contact firstMergeable(List<Contact> candidates, ContactRequest req, String callerId) {
        return candidates.stream()
                .filter(candidate -> !req.isPrivate() || Objects.equals(candidate.getCreatedBy(), callerId))
                .findFirst()
                .orElse(null);
    }

    public Contact update(String id, ContactRequest req) {
        Contact contact = requireVisible(id);
        contact.setName(req.getName());
        contact.setCompany(req.getCompany());
        contact.setMobile(req.getMobile());
        contact.setEmails(req.getEmails());
        contact.setLinkedinProfile(req.getLinkedinProfile());
        // verified is server-owned and deliberately left as it is — see create().
        // isPrivate likewise stays put unless the owner is the one editing.
        if (Objects.equals(contact.getCreatedBy(), currentUser.requireId())) {
            contact.setPrivate(req.isPrivate());
        }
        contact.setUpdatedAt(Instant.now());
        return repository.save(contact);
    }

    public void softDelete(String id) {
        Contact contact = requireVisible(id);
        contact.setDeleted(true);
        contact.setUpdatedAt(Instant.now());
        repository.save(contact);
    }

    public BatchSyncResponse batchSync(BatchSyncRequest req) {
        List<Contact> upserted = new ArrayList<>();
        List<String> deletedIds = new ArrayList<>();
        long serverTimestamp = System.currentTimeMillis();
        String callerId = currentUser.requireId();

        if (req.getChanges() == null) {
            return new BatchSyncResponse(serverTimestamp, upserted, deletedIds);
        }

        for (SyncChange change : req.getChanges()) {
            if (change.getAction() == null) continue;

            switch (change.getAction().toUpperCase()) {
                case "CREATE" -> {
                    if (change.getContact() != null) {
                        upserted.add(create(change.getContact()));
                    }
                }
                case "UPDATE" -> {
                    if (change.getServerId() != null && change.getContact() != null) {
                        Contact existing = repository.findById(change.getServerId()).orElse(null);
                        if (existing == null) {
                            // Treat as CREATE if the server ID is unknown
                            upserted.add(create(change.getContact()));
                        } else if (!isVisibleTo(existing, callerId)) {
                            // The owner turned it private after this client had already synced it.
                            // Letting update() throw would fail the entire batch on every retry, so
                            // answer with the tombstone instead: the client purges its copy and stops
                            // pushing the edit. Still one entry, since the client pairs the response
                            // with its own pending rows by position.
                            upserted.add(tombstone(existing));
                        } else {
                            // Last-write-wins: apply only if client change is newer
                            Instant clientUpdatedAt = change.getClientUpdatedAt() != null
                                    ? Instant.ofEpochMilli(change.getClientUpdatedAt())
                                    : Instant.now();
                            if (existing.getUpdatedAt() == null || clientUpdatedAt.isAfter(existing.getUpdatedAt())) {
                                upserted.add(update(change.getServerId(), change.getContact()));
                            } else {
                                // Server is newer — return current server state so client reconciles
                                upserted.add(existing);
                            }
                        }
                    }
                }
                case "DELETE" -> {
                    if (change.getServerId() != null) {
                        Contact existing = repository.findById(change.getServerId()).orElse(null);
                        if (existing != null && !isVisibleTo(existing, callerId)) {
                            // As in UPDATE above: report it as gone so the client drops the stale
                            // copy, and leave the owner's record alone.
                            deletedIds.add(change.getServerId());
                        } else if (existing != null && !existing.isDeleted()) {
                            Instant clientUpdatedAt = change.getClientUpdatedAt() != null
                                    ? Instant.ofEpochMilli(change.getClientUpdatedAt())
                                    : Instant.now();
                            if (existing.getUpdatedAt() == null || clientUpdatedAt.isAfter(existing.getUpdatedAt())) {
                                softDelete(change.getServerId());
                                deletedIds.add(change.getServerId());
                            }
                        }
                    }
                }
            }
        }

        return new BatchSyncResponse(serverTimestamp, upserted, deletedIds);
    }
}
