package gabri.dev.chatapp.websocket;

import gabri.dev.chatapp.dtos.websocket.UserStatusWS;
import gabri.dev.chatapp.entities.User;
import gabri.dev.chatapp.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

/**
 * Listener para eventos de conexión y desconexión de WebSocket.
 * Actualiza el estado del usuario automáticamente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;

    /**
     * Se ejecuta cuando un usuario se conecta al WebSocket.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            log.info("Usuario conectado via WebSocket: {}", username);

            try {
                // Actualizar estado a ONLINE
                User user = userService.getUserEntityById(
                        userService.getUserByUsername(username).getId()
                );

                UserStatusWS statusWS = UserStatusWS.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .status(User.UserStatus.ONLINE)
                        .timestamp(LocalDateTime.now())
                        .build();

                // Notificar a todos los usuarios del cambio de estado
                messagingTemplate.convertAndSend("/topic/user-status", statusWS);

            } catch (Exception e) {
                log.error("Error actualizando estado de usuario en conexión: {}", e.getMessage());
            }
        }
    }

    /**
     * Se ejecuta cuando un usuario se desconecta del WebSocket.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            log.info("Usuario desconectado del WebSocket: {}", username);

            try {
                // Actualizar estado a OFFLINE
                User user = userService.getUserEntityById(
                        userService.getUserByUsername(username).getId()
                );

                UserStatusWS statusWS = UserStatusWS.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .status(User.UserStatus.OFFLINE)
                        .timestamp(LocalDateTime.now())
                        .build();

                // Notificar a todos los usuarios del cambio de estado
                messagingTemplate.convertAndSend("/topic/user-status", statusWS);

            } catch (Exception e) {
                log.error("Error actualizando estado de usuario en desconexión: {}", e.getMessage());
            }
        }
    }
}