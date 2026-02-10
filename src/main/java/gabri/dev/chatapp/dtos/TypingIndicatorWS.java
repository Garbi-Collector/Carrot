package gabri.dev.chatapp.dtos.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para indicador de "está escribiendo" por WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingIndicatorWS {

    private Long chatRoomId;
    private Long userId;
    private String username;
    private Boolean isTyping;
}