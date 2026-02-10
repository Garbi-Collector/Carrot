package gabri.dev.chatapp.dtos.websocket;

import gabri.dev.chatapp.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mensajes transmitidos por WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageWS {

    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private String content;
    private Message.MessageType type;
    private LocalDateTime sentAt;
    private Boolean isEdited;
}