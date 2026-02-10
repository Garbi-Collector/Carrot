package gabri.dev.chatapp.controllers;

import gabri.dev.chatapp.dtos.*;
import gabri.dev.chatapp.services.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de salas de chat.
 */
@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Salas de Chat", description = "Endpoints para gestión de salas de chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * Obtiene todas las salas de chat del usuario actual.
     */
    @GetMapping
    @Operation(summary = "Listar salas", description = "Obtiene todas las salas de chat del usuario actual")
    public ResponseEntity<ApiResponseDTO<List<ChatRoomDTO>>> getCurrentUserChatRooms() {

        log.info("Request para obtener salas de chat del usuario actual");

        List<ChatRoomDTO> chatRooms = chatRoomService.getCurrentUserChatRooms();

        return ResponseEntity.ok(ApiResponseDTO.success(chatRooms, "Salas obtenidas exitosamente"));
    }

    /**
     * Obtiene una sala de chat por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener sala por ID", description = "Obtiene información de una sala específica")
    public ResponseEntity<ApiResponseDTO<ChatRoomDTO>> getChatRoomById(@PathVariable Long id) {

        log.info("Request para obtener sala de chat con ID: {}", id);

        ChatRoomDTO chatRoom = chatRoomService.getChatRoomById(id);

        return ResponseEntity.ok(ApiResponseDTO.success(chatRoom, "Sala obtenida exitosamente"));
    }

    /**
     * Crea una nueva sala de chat grupal.
     */
    @PostMapping("/group")
    @Operation(summary = "Crear sala grupal", description = "Crea una nueva sala de chat grupal")
    public ResponseEntity<ApiResponseDTO<ChatRoomDTO>> createGroupChatRoom(
            @Valid @RequestBody ChatRoomCreateDTO createDTO) {

        log.info("Request para crear sala de chat grupal: {}", createDTO.getName());

        ChatRoomDTO chatRoom = chatRoomService.createGroupChatRoom(createDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(chatRoom, "Sala grupal creada exitosamente"));
    }

    /**
     * Crea o recupera una sala de chat privada.
     */
    @PostMapping("/private")
    @Operation(summary = "Crear/obtener chat privado", description = "Crea o recupera una sala de chat privada entre dos usuarios")
    public ResponseEntity<ApiResponseDTO<ChatRoomDTO>> createOrGetPrivateChatRoom(
            @Valid @RequestBody PrivateChatRoomCreateDTO createDTO) {

        log.info("Request para crear/obtener chat privado con usuario ID: {}", createDTO.getRecipientId());

        ChatRoomDTO chatRoom = chatRoomService.createOrGetPrivateChatRoom(createDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(chatRoom, "Chat privado obtenido exitosamente"));
    }

    /**
     * Actualiza una sala de chat grupal.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sala", description = "Actualiza la información de una sala grupal")
    public ResponseEntity<ApiResponseDTO<ChatRoomDTO>> updateChatRoom(
            @PathVariable Long id,
            @Valid @RequestBody ChatRoomUpdateDTO updateDTO) {

        log.info("Request para actualizar sala de chat con ID: {}", id);

        ChatRoomDTO chatRoom = chatRoomService.updateChatRoom(id, updateDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(chatRoom, "Sala actualizada exitosamente"));
    }

    /**
     * Agrega un participante a una sala grupal.
     */
    @PostMapping("/{id}/participants/{userId}")
    @Operation(summary = "Agregar participante", description = "Agrega un usuario a una sala grupal")
    public ResponseEntity<ApiResponseDTO<ChatRoomDTO>> addParticipant(
            @PathVariable Long id,
            @PathVariable Long userId) {

        log.info("Request para agregar usuario {} a sala {}", userId, id);

        ChatRoomDTO chatRoom = chatRoomService.addParticipant(id, userId);

        return ResponseEntity.ok(ApiResponseDTO.success(chatRoom, "Participante agregado exitosamente"));
    }

    /**
     * Remueve un participante de una sala grupal.
     */
    @DeleteMapping("/{id}/participants/{userId}")
    @Operation(summary = "Remover participante", description = "Remueve un usuario de una sala grupal")
    public ResponseEntity<ApiResponseDTO<ChatRoomDTO>> removeParticipant(
            @PathVariable Long id,
            @PathVariable Long userId) {

        log.info("Request para remover usuario {} de sala {}", userId, id);

        ChatRoomDTO chatRoom = chatRoomService.removeParticipant(id, userId);

        return ResponseEntity.ok(ApiResponseDTO.success(chatRoom, "Participante removido exitosamente"));
    }

    /**
     * Abandona una sala de chat.
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "Abandonar sala", description = "El usuario actual abandona una sala de chat")
    public ResponseEntity<ApiResponseDTO<Void>> leaveChatRoom(@PathVariable Long id) {

        log.info("Request para abandonar sala de chat con ID: {}", id);

        chatRoomService.leaveChatRoom(id);

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Has abandonado la sala exitosamente"));
    }

    /**
     * Elimina una sala de chat grupal.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sala", description = "Elimina una sala grupal (solo creador)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteChatRoom(@PathVariable Long id) {

        log.info("Request para eliminar sala de chat con ID: {}", id);

        chatRoomService.deleteChatRoom(id);

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Sala eliminada exitosamente"));
    }

    /**
     * Busca salas de chat por nombre.
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar salas", description = "Busca salas de chat por nombre")
    public ResponseEntity<ApiResponseDTO<List<ChatRoomDTO>>> searchChatRooms(
            @RequestParam String query) {

        log.info("Request para buscar salas con query: {}", query);

        List<ChatRoomDTO> chatRooms = chatRoomService.searchChatRooms(query);

        return ResponseEntity.ok(ApiResponseDTO.success(chatRooms, "Búsqueda completada exitosamente"));
    }
}