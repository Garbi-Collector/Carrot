package gabri.dev.chatapp.controllers;

import gabri.dev.chatapp.dtos.*;
import gabri.dev.chatapp.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de mensajes.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mensajes", description = "Endpoints para gestión de mensajes")
public class MessageController {

    private final MessageService messageService;

    /**
     * Envía un mensaje a una sala de chat.
     */
    @PostMapping
    @Operation(summary = "Enviar mensaje", description = "Envía un nuevo mensaje a una sala de chat")
    public ResponseEntity<ApiResponseDTO<MessageDTO>> sendMessage(
            @Valid @RequestBody MessageSendDTO sendDTO) {

        log.info("Request para enviar mensaje a sala {}", sendDTO.getChatRoomId());

        MessageDTO message = messageService.sendMessage(sendDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(message, "Mensaje enviado exitosamente"));
    }

    /**
     * Obtiene mensajes de una sala con paginación.
     */
    @GetMapping("/chatroom/{chatRoomId}")
    @Operation(summary = "Obtener mensajes", description = "Obtiene mensajes de una sala con paginación")
    public ResponseEntity<ApiResponseDTO<Page<MessageDTO>>> getMessagesByChatRoom(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Request para obtener mensajes de sala {} - página {}", chatRoomId, page);

        Page<MessageDTO> messages = messageService.getMessagesByChatRoom(chatRoomId, page, size);

        return ResponseEntity.ok(ApiResponseDTO.success(messages, "Mensajes obtenidos exitosamente"));
    }

    /**
     * Obtiene los últimos N mensajes de una sala.
     */
    @GetMapping("/chatroom/{chatRoomId}/recent")
    @Operation(summary = "Últimos mensajes", description = "Obtiene los últimos N mensajes de una sala")
    public ResponseEntity<ApiResponseDTO<List<MessageDTO>>> getLastMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "50") int limit) {

        log.info("Request para obtener últimos {} mensajes de sala {}", limit, chatRoomId);

        List<MessageDTO> messages = messageService.getLastMessages(chatRoomId, limit);

        return ResponseEntity.ok(ApiResponseDTO.success(messages, "Mensajes obtenidos exitosamente"));
    }

    /**
     * Obtiene un mensaje por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener mensaje por ID", description = "Obtiene información de un mensaje específico")
    public ResponseEntity<ApiResponseDTO<MessageDTO>> getMessageById(@PathVariable Long id) {

        log.info("Request para obtener mensaje con ID: {}", id);

        MessageDTO message = messageService.getMessageById(id);

        return ResponseEntity.ok(ApiResponseDTO.success(message, "Mensaje obtenido exitosamente"));
    }

    /**
     * Edita un mensaje existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Editar mensaje", description = "Edita el contenido de un mensaje (solo el autor)")
    public ResponseEntity<ApiResponseDTO<MessageDTO>> editMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageEditDTO editDTO) {

        log.info("Request para editar mensaje con ID: {}", id);

        MessageDTO message = messageService.editMessage(id, editDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(message, "Mensaje editado exitosamente"));
    }

    /**
     * Elimina un mensaje.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar mensaje", description = "Elimina un mensaje (solo el autor)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteMessage(@PathVariable Long id) {

        log.info("Request para eliminar mensaje con ID: {}", id);

        messageService.deleteMessage(id);

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Mensaje eliminado exitosamente"));
    }

    /**
     * Busca mensajes por contenido en una sala.
     */
    @GetMapping("/chatroom/{chatRoomId}/search")
    @Operation(summary = "Buscar mensajes", description = "Busca mensajes por contenido en una sala")
    public ResponseEntity<ApiResponseDTO<List<MessageDTO>>> searchMessages(
            @PathVariable Long chatRoomId,
            @RequestParam String query) {

        log.info("Request para buscar mensajes en sala {} con query: {}", chatRoomId, query);

        List<MessageDTO> messages = messageService.searchMessages(chatRoomId, query);

        return ResponseEntity.ok(ApiResponseDTO.success(messages, "Búsqueda completada exitosamente"));
    }

    /**
     * Obtiene el conteo de mensajes en una sala.
     */
    @GetMapping("/chatroom/{chatRoomId}/count")
    @Operation(summary = "Contar mensajes", description = "Obtiene el número total de mensajes en una sala")
    public ResponseEntity<ApiResponseDTO<Long>> getMessageCount(@PathVariable Long chatRoomId) {

        log.info("Request para contar mensajes de sala {}", chatRoomId);

        Long count = messageService.getMessageCount(chatRoomId);

        return ResponseEntity.ok(ApiResponseDTO.success(count, "Conteo obtenido exitosamente"));
    }
}