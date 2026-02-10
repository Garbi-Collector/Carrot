package gabri.dev.chatapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de error detalladas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

    private Integer status;
    private String error;
    private String message;
    private String path;
    private List<String> details;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}