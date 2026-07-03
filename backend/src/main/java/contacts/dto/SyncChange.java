package contacts.dto;

import lombok.Data;

@Data
public class SyncChange {

    // "CREATE", "UPDATE", or "DELETE"
    private String action;

    // Server-assigned ID — required for UPDATE and DELETE, null for CREATE
    private String serverId;

    // Contact payload — required for CREATE and UPDATE, null for DELETE
    private ContactRequest contact;

    // Client-side updatedAt (epoch millis) used for last-write-wins conflict resolution
    private Long clientUpdatedAt;
}
