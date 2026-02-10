package gabri.dev.chatapp.dtos;

import gabri.dev.chatapp.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de usuario.
 * No incluye información sensible como la contraseña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private User.UserStatus status;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
}