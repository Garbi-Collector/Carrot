package gabri.dev.chatapp.dtos;

import gabri.dev.chatapp.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de mensajes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    private Long id;
    private String content;
    private Message.MessageType type;
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private Boolean isEdited;
    private UserDTO sender;
    private Long chatRoomId;
}