package gabri.dev.chatapp.websocket;

import gabri.dev.chatapp.dtos.websocket.ChatMessageWS;
import gabri.dev.chatapp.dtos.websocket.UserStatusWS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

/**
 * Servicio para enviar notificaciones via WebSocket desde cualquier parte de la aplicación.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Envía un mensaje a una sala de chat específica.
     */
    public void sendMessageToRoom(Long chatRoomId, ChatMessageWS message) {
        log.debug("Enviando mensaje a sala {} via WebSocket", chatRoomId);
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, message);
    }

    /**
     * Notifica cambio de estado de usuario a todos.
     */
    public void notifyUserStatusChange(UserStatusWS userStatus) {
        log.debug("Notificando cambio de estado de usuario {} a {}",
                userStatus.getUsername(), userStatus.getStatus());
        messagingTemplate.convertAndSend("/topic/user-status", userStatus);
    }

    /**
     * Notifica a un usuario específico (mensaje privado).
     */
    public void sendPrivateNotification(String username, Object notification) {
        log.debug("Enviando notificación privada a usuario {}", username);
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
    }

    /**
     * Broadcast general a todos los usuarios conectados.
     */
    public void broadcastToAll(String destination, Object payload) {
        log.debug("Broadcasting a {}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }
}