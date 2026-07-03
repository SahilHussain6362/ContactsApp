package contacts.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchSyncRequest {

    private List<SyncChange> changes;
}
