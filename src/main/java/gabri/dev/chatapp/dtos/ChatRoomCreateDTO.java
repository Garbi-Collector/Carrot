package gabri.dev.chatapp.dtos;

import gabri.dev.chatapp.entities.ChatRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para crear una nueva sala de chat.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateDTO {

    @NotBlank(message = "El nombre de la sala es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de sala es obligatorio")
    private ChatRoom.ChatRoomType type;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String imageUrl;

    // Lista de IDs de usuarios a agregar como participantes
    private List<Long> participantIds;
}