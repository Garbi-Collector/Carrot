package gabri.dev.chatapp.dtos;

import gabri.dev.chatapp.entities.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferir información de salas de chat.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {

    private Long id;
    private String name;
    private ChatRoom.ChatRoomType type;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO createdBy;
    private List<UserDTO> participants;
    private MessageDTO lastMessage;
    private Integer participantCount;
    private Long unreadCount; // Mensajes no leídos (para futuras implementaciones)
}