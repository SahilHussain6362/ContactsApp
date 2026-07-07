package contacts.dto;

import contacts.model.AuthProvider;
import contacts.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;
    private String name;
    private String email;
    private AuthProvider provider;
    private Instant createdAt;

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getProvider(), user.getCreatedAt());
    }
}
