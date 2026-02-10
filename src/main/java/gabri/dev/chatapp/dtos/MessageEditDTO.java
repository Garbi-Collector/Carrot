package gabri.dev.chatapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para editar un mensaje existente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEditDTO {

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 5000, message = "El mensaje no puede exceder 5000 caracteres")
    private String content;
}