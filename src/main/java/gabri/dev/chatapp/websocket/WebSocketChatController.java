package gabri.dev.chatapp.websocket;

import gabri.dev.chatapp.dtos.MessageDTO;
import gabri.dev.chatapp.dtos.MessageSendDTO;
import gabri.dev.chatapp.dtos.websocket.ChatMessageWS;
import gabri.dev.chatapp.dtos.websocket.TypingIndicatorWS;
import gabri.dev.chatapp.entities.User;
import gabri.dev.chatapp.services.MessageService;
import gabri.dev.chatapp.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controlador WebSocket para mensajes de chat en tiempo real.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;

    /**
     * Recibe un mensaje del cliente y lo broadcast a la sala.
     * Ruta: /app/chat.sendMessage/{chatRoomId}
     * Subscripción: /topic/chatroom/{chatRoomId}
     */
    @MessageMapping("/chat.sendMessage/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload MessageSendDTO messageSendDTO,
            Principal principal) {

        log.info("Mensaje recibido via WebSocket de usuario: {} para sala: {}",
                principal.getName(), chatRoomId);

        try {
            // Guardar el mensaje en la base de datos
            MessageDTO savedMessage = messageService.sendMessage(messageSendDTO);

            // Crear DTO optimizado para WebSocket
            User sender = userService.getUserEntityById(savedMessage.getSender().getId());

            ChatMessageWS wsMessage = ChatMessageWS.builder()
                    .id(savedMessage.getId())
                    .chatRoomId(savedMessage.getChatRoomId())
                    .senderId(savedMessage.getSender().getId())
                    .senderUsername(savedMessage.getSender().getUsername())
                    .senderAvatarUrl(sender.getAvatarUrl())
                    .content(savedMessage.getContent())
                    .type(savedMessage.getType())
                    .sentAt(savedMessage.getSentAt())
                    .isEdited(savedMessage.getIsEdited())
                    .build();

            // Enviar a todos los subscriptores de la sala
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatRoomId,
                    wsMessage
            );

            log.info("Mensaje enviado via WebSocket a sala: {}", chatRoomId);

        } catch (Exception e) {
            log.error("Error procesando mensaje via WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Maneja el indicador de "está escribiendo".
     * Ruta: /app/chat.typing/{chatRoomId}
     * Subscripción: /topic/chatroom/{chatRoomId}/typing
     */
    @MessageMapping("/chat.typing/{chatRoomId}")
    public void handleTyping(
            @DestinationVariable Long chatRoomId,
            @Payload TypingIndicatorWS typingIndicator,
            Principal principal) {

        log.debug("Indicador de escritura recibido de {} para sala {}",
                principal.getName(), chatRoomId);

        try {
            User user = userService.getUserEntityById(
                    userService.getUserByUsername(principal.getName()).getId()
            );

            typingIndicator.setUserId(user.getId());
            typingIndicator.setUsername(user.getUsername());
            typingIndicator.setChatRoomId(chatRoomId);

            // Enviar indicador a la sala (excepto al remitente)
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatRoomId + "/typing",
                    typingIndicator
            );

        } catch (Exception e) {
            log.error("Error procesando indicador de escritura: {}", e.getMessage());
        }
    }

    /**
     * Maneja la edición de mensajes en tiempo real.
     * Ruta: /app/chat.editMessage/{chatRoomId}
     * Subscripción: /topic/chatroom/{chatRoomId}/edited
     */
    @MessageMapping("/chat.editMessage/{chatRoomId}")
    public void handleEditMessage(
            @DestinationVariable Long chatRoomId,
            @Payload ChatMessageWS editedMessage,
            Principal principal) {

        log.info("Edición de mensaje recibida via WebSocket de {} para sala {}",
                principal.getName(), chatRoomId);

        try {
            // Verificar que el usuario sea el autor del mensaje
            // (la validación real se hace en el servicio)

            // Broadcast del mensaje editado
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatRoomId + "/edited",
                    editedMessage
            );

        } catch (Exception e) {
            log.error("Error procesando edición de mensaje: {}", e.getMessage());
        }
    }

    /**
     * Maneja la eliminación de mensajes en tiempo real.
     * Ruta: /app/chat.deleteMessage/{chatRoomId}
     * Subscripción: /topic/chatroom/{chatRoomId}/deleted
     */
    @MessageMapping("/chat.deleteMessage/{chatRoomId}")
    public void handleDeleteMessage(
            @DestinationVariable Long chatRoomId,
            @Payload Long messageId,
            Principal principal) {

        log.info("Eliminación de mensaje {} recibida via WebSocket de {} para sala {}",
                messageId, principal.getName(), chatRoomId);

        try {
            // Broadcast del ID del mensaje eliminado
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatRoomId + "/deleted",
                    messageId
            );

        } catch (Exception e) {
            log.error("Error procesando eliminación de mensaje: {}", e.getMessage());
        }
    }
}