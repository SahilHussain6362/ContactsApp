package contacts.service;

import contacts.dto.BatchSyncRequest;
import contacts.dto.BatchSyncResponse;
import contacts.dto.ContactRequest;
import contacts.dto.SyncChange;
import contacts.model.HrContact;
import contacts.repository.HrContactRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class HrContactService {

    private final HrContactRepository repository;

    public HrContactService(HrContactRepository repository) {
        this.repository = repository;
    }

    public List<HrContact> getAllContacts() {
        return repository.findByDeletedFalseOrderByCompanyAscNameAsc();
    }

    public List<HrContact> getChangesSince(long epochMillis) {
        return repository.findByUpdatedAtAfter(Instant.ofEpochMilli(epochMillis));
    }

    public HrContact create(ContactRequest req) {
        // If a non-deleted contact already exists with the same email, mobile, or
        // LinkedIn profile, update that record instead of creating a duplicate.
        HrContact existing = findDuplicate(req);
        if (existing != null) {
            if(Objects.isNull(existing.getMobile())) existing.setMobile(req.getMobile());

            Set<String> uniqueEmails = new LinkedHashSet<>(existing.getEmails());
            uniqueEmails.addAll(req.getEmails());
            existing.setEmails(new ArrayList<>(uniqueEmails));

            if(Objects.isNull(existing.getLinkedinProfile())) existing.setLinkedinProfile(req.getLinkedinProfile());
            existing.setUpdatedAt(Instant.now());
            return repository.save(existing);
        }

        Instant now = Instant.now();
        HrContact contact = new HrContact();
        contact.setName(req.getName());
        contact.setCompany(req.getCompany());
        contact.setMobile(req.getMobile());
        contact.setEmails(req.getEmails());
        contact.setLinkedinProfile(req.getLinkedinProfile());
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);
        contact.setDeleted(false);
        return repository.save(contact);
    }

    // Returns the first existing non-deleted contact that shares any email, mobile,
    // or linkedinProfile with the request. Emails take priority, then mobile, then LinkedIn.
    private HrContact findDuplicate(ContactRequest req) {

        String mobile = req.getMobile();
        if (mobile != null && !mobile.isBlank()) {
            var match = repository.findFirstByMobileAndDeletedFalse(mobile);
            if (match.isPresent()) return match.get();
        }

        String linkedin = req.getLinkedinProfile();
        if (linkedin != null && !linkedin.isBlank()) {
            var match = repository.findFirstByLinkedinProfileAndDeletedFalse(linkedin);
            if (match.isPresent()) return match.get();
        }

        return null;
    }

    public HrContact update(String id, ContactRequest req) {
        HrContact contact = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + id));
        contact.setName(req.getName());
        contact.setCompany(req.getCompany());
        contact.setMobile(req.getMobile());
        contact.setEmails(req.getEmails());
        contact.setLinkedinProfile(req.getLinkedinProfile());
        contact.setUpdatedAt(Instant.now());
        return repository.save(contact);
    }

    public void softDelete(String id) {
        HrContact contact = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + id));
        contact.setDeleted(true);
        contact.setUpdatedAt(Instant.now());
        repository.save(contact);
    }

    public BatchSyncResponse batchSync(BatchSyncRequest req) {
        List<HrContact> upserted = new ArrayList<>();
        List<String> deletedIds = new ArrayList<>();
        long serverTimestamp = System.currentTimeMillis();

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
                        HrContact existing = repository.findById(change.getServerId()).orElse(null);
                        if (existing == null) {
                            // Treat as CREATE if the server ID is unknown
                            upserted.add(create(change.getContact()));
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
                        HrContact existing = repository.findById(change.getServerId()).orElse(null);
                        if (existing != null && !existing.isDeleted()) {
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
