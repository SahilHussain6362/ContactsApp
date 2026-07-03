package contacts.dto;

import contacts.model.HrContact;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchSyncResponse {

    // Epoch millis — client stores this as the new lastSync timestamp
    private long serverTimestamp;

    // Contacts that were created or updated during this batch
    private List<HrContact> contacts;

    // Server IDs of contacts that were soft-deleted during this batch
    private List<String> deletedIds;
}
