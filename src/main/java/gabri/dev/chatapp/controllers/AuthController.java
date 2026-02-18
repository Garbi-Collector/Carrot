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
@Tag(name = "Autenticación", description = "Endpoints para registro, login y verificación de usuarios")
public class AuthController {

    private final UserService userService;

    /**
     * Registra un nuevo usuario y envía email de verificación.
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario",
            description = "Registra un nuevo usuario y envía email de verificación")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> register(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Request de registro recibido para username: {}", registrationDTO.getUsername());
        AuthResponseDTO response = userService.register(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response,
                        "Usuario registrado. Por favor verifica tu email para activar tu cuenta."));
    }

    /**
     * Verifica el email del usuario mediante el token.
     */
    @GetMapping("/verify")
    @Operation(summary = "Verificar email",
            description = "Verifica el email del usuario usando el token enviado por correo")
    public ResponseEntity<ApiResponseDTO<Void>> verifyEmail(@RequestParam("token") String token) {
        log.info("Request de verificación de email recibido");
        userService.verifyEmail(token);
        return ResponseEntity.ok(
                ApiResponseDTO.success(null, "Email verificado exitosamente. Ya puedes iniciar sesión."));
    }

    /**
     * Reenvía el email de verificación.
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Reenviar email de verificación",
            description = "Reenvía el email de verificación a un usuario no verificado")
    public ResponseEntity<ApiResponseDTO<Void>> resendVerificationEmail(
            @RequestParam("email") String email) {
        log.info("Request de reenvío de verificación para: {}", email);
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok(
                ApiResponseDTO.success(null, "Email de verificación reenviado."));
    }

    /**
     * Inicia sesión de un usuario (solo si el email está verificado).
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Autentica un usuario verificado y retorna un token JWT")
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