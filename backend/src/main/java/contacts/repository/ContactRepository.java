package contacts.repository;

import contacts.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends MongoRepository<Contact, String> {

    // Each query below repeats the same visibility rule: a contact is readable when it is shared,
    // or when the caller created it. The caller id is always ?0 so the clause reads identically in
    // all three. `$ne: true` rather than `false` so documents written before the isPrivate field
    // existed — where the key is simply absent — still count as shared.
    @Query(value = "{ 'deleted': false, $or: [ { 'isPrivate': { $ne: true } }, { 'createdBy': ?0 } ] }",
            sort = "{ 'company': 1, 'name': 1 }")
    List<Contact> findVisibleTo(String callerId);

    List<Contact> findByCreatedByAndDeletedFalseOrderByCompanyAscNameAsc(String createdBy);

    // Used for incremental sync — returns both live and soft-deleted records
    // so clients learn what to purge locally.
    List<Contact> findByUpdatedAtAfter(Instant since);

    // Duplicate detection — @Query is required here because Spring Data cannot derive
    // a "contains single element" query from a List<String> field via method naming.
    // MongoDB's { emails: <value> } query matches documents where the array contains that value.
    @Query("{ 'emails': ?0, 'deleted': false }")
    Optional<Contact> findFirstByEmailContained(String email);

    // Deliberately scoped to what the caller can see: merging a create into another user's private
    // contact would both expose and mutate a record the caller was never allowed to read. All
    // matches are returned rather than just the first so the service can skip candidates it may
    // see but must not merge into.
    @Query("{ 'mobile': ?1, 'deleted': false, $or: [ { 'isPrivate': { $ne: true } }, { 'createdBy': ?0 } ] }")
    List<Contact> findVisibleByMobile(String callerId, String mobile);

    @Query("{ 'linkedinProfile': ?1, 'deleted': false, $or: [ { 'isPrivate': { $ne: true } }, { 'createdBy': ?0 } ] }")
    List<Contact> findVisibleByLinkedinProfile(String callerId, String linkedinProfile);
}
