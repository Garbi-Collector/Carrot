package gabri.dev.chatapp.controllers;

import gabri.dev.chatapp.dtos.*;
import gabri.dev.chatapp.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para endpoints de autenticación (registro y login).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para registro y login de usuarios")
public class AuthController {

    private final UserService userService;

    /**
     * Registra un nuevo usuario.
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> register(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {

        log.info("Request de registro recibido para username: {}", registrationDTO.getUsername());

        AuthResponseDTO response = userService.register(registrationDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response, "Usuario registrado exitosamente"));
    }

    /**
     * Inicia sesión de un usuario.
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(
            @Valid @RequestBody UserLoginDTO loginDTO) {

        log.info("Request de login recibido para: {}", loginDTO.getUsernameOrEmail());

        AuthResponseDTO response = userService.login(loginDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Login exitoso"));
    }

    /**
     * Cierra sesión del usuario actual.
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario actual")
    public ResponseEntity<ApiResponseDTO<Void>> logout() {

        log.info("Request de logout recibido");

        userService.logout();

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Sesión cerrada exitosamente"));
    }

    /**
     * Obtiene información del usuario actual autenticado.
     */
    @GetMapping("/me")
    @Operation(summary = "Usuario actual", description = "Obtiene información del usuario autenticado")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getCurrentUser() {

        UserDTO user = userService.getCurrentUserDTO();

        return ResponseEntity.ok(ApiResponseDTO.success(user, "Usuario obtenido exitosamente"));
    }
}