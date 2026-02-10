package gabri.dev.chatapp.dtos.websocket;

import gabri.dev.chatapp.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para notificaciones de cambio de estado por WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusWS {

    private Long userId;
    private String username;
    private User.UserStatus status;
    private LocalDateTime timestamp;
}