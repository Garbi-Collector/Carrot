package gabri.dev.chatapp.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una sala de chat privada (1 a 1).
 * Solo requiere el ID del otro usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateChatRoomCreateDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long recipientId;
}