package contacts.repository;

import contacts.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends MongoRepository<Contact, String> {

    List<Contact> findByDeletedFalseOrderByCompanyAscNameAsc();

    // Used for incremental sync — returns both live and soft-deleted records
    // so clients learn what to purge locally.
    List<Contact> findByUpdatedAtAfter(Instant since);

    // Duplicate detection — @Query is required here because Spring Data cannot derive
    // a "contains single element" query from a List<String> field via method naming.
    // MongoDB's { emails: <value> } query matches documents where the array contains that value.
    @Query("{ 'emails': ?0, 'deleted': false }")
    Optional<Contact> findFirstByEmailContained(String email);

    Optional<Contact> findFirstByMobileAndDeletedFalse(String mobile);

    Optional<Contact> findFirstByLinkedinProfileAndDeletedFalse(String linkedinProfile);
}
