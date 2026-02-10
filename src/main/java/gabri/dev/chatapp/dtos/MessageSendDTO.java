package gabri.dev.chatapp.dtos;

import gabri.dev.chatapp.entities.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar un nuevo mensaje.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageSendDTO {

    @NotNull(message = "El ID de la sala es obligatorio")
    private Long chatRoomId;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 5000, message = "El mensaje no puede exceder 5000 caracteres")
    private String content;

    @Builder.Default
    private Message.MessageType type = Message.MessageType.CHAT;
}