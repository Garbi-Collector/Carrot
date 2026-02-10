package gabri.dev.chatapp.controllers;

import gabri.dev.chatapp.dtos.*;
import gabri.dev.chatapp.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de usuarios.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios")
public class UserController {

    private final UserService userService;

    /**
     * Obtiene todos los usuarios (excepto el actual).
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene la lista de todos los usuarios excepto el actual")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getAllUsers() {

        log.info("Request para obtener todos los usuarios");

        List<UserDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(ApiResponseDTO.success(users, "Usuarios obtenidos exitosamente"));
    }

    /**
     * Obtiene un usuario por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene información de un usuario específico")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserById(@PathVariable Long id) {

        log.info("Request para obtener usuario con ID: {}", id);

        UserDTO user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponseDTO.success(user, "Usuario obtenido exitosamente"));
    }

    /**
     * Busca usuarios por username.
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por username")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> searchUsers(
            @RequestParam String query) {

        log.info("Request para buscar usuarios con query: {}", query);

        List<UserDTO> users = userService.searchUsers(query);

        return ResponseEntity.ok(ApiResponseDTO.success(users, "Búsqueda completada exitosamente"));
    }

    /**
     * Obtiene usuarios online.
     */
    @GetMapping("/online")
    @Operation(summary = "Usuarios online", description = "Obtiene la lista de usuarios conectados")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getOnlineUsers() {

        log.info("Request para obtener usuarios online");

        List<UserDTO> users = userService.getOnlineUsers();

        return ResponseEntity.ok(ApiResponseDTO.success(users, "Usuarios online obtenidos exitosamente"));
    }

    /**
     * Actualiza información del usuario actual.
     */
    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil", description = "Actualiza la información del usuario actual")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateCurrentUser(
            @Valid @RequestBody UserUpdateDTO updateDTO) {

        log.info("Request para actualizar usuario actual");

        UserDTO user = userService.updateCurrentUser(updateDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(user, "Usuario actualizado exitosamente"));
    }

    /**
     * Actualiza el estado del usuario actual.
     */
    @PutMapping("/me/status")
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de conexión del usuario")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateUserStatus(
            @Valid @RequestBody UserStatusDTO statusDTO) {

        log.info("Request para actualizar estado de usuario");

        UserDTO user = userService.updateUserStatus(statusDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(user, "Estado actualizado exitosamente"));
    }
}