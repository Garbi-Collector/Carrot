package gabri.dev.chatapp.dtos;

import gabri.dev.chatapp.entities.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar el estado de conexión del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusDTO {

    @NotNull(message = "El estado es obligatorio")
    private User.UserStatus status;
}