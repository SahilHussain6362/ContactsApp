package contacts.repository;

import contacts.model.HrContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HrContactRepository extends MongoRepository<HrContact, String> {

    List<HrContact> findByDeletedFalseOrderByCompanyAscNameAsc();

    // Used for incremental sync — returns both live and soft-deleted records
    // so clients learn what to purge locally.
    List<HrContact> findByUpdatedAtAfter(Instant since);

    // Duplicate detection — @Query is required here because Spring Data cannot derive
    // a "contains single element" query from a List<String> field via method naming.
    // MongoDB's { emails: <value> } query matches documents where the array contains that value.
    @Query("{ 'emails': ?0, 'deleted': false }")
    Optional<HrContact> findFirstByEmailContained(String email);

    Optional<HrContact> findFirstByMobileAndDeletedFalse(String mobile);

    Optional<HrContact> findFirstByLinkedinProfileAndDeletedFalse(String linkedinProfile);
}
