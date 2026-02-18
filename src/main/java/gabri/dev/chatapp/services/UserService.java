package gabri.dev.chatapp.services;

import gabri.dev.chatapp.dtos.*;
import gabri.dev.chatapp.entities.User;
import gabri.dev.chatapp.entities.VerificationToken;
import gabri.dev.chatapp.exceptions.*;
import gabri.dev.chatapp.repositories.UserRepository;
import gabri.dev.chatapp.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    @Qualifier("mergerMapper")
    private final ModelMapper mergerMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;

    /**
     * Registra un nuevo usuario y envía email de verificación.
     */
    @Transactional
    public AuthResponseDTO register(UserRegistrationDTO registrationDTO) {
        log.info("Intentando registrar usuario: {}", registrationDTO.getUsername());

        // Verificar si el username ya existe
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new UserAlreadyExistsException("username", registrationDTO.getUsername());
        }

        // Verificar si el email ya existe
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new UserAlreadyExistsException("email", registrationDTO.getEmail());
        }

        // Crear el usuario (SIN VERIFICAR y DESHABILITADO)
        User user = User.builder()
                .username(registrationDTO.getUsername())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .fullName(registrationDTO.getFullName())
                .status(User.UserStatus.OFFLINE)
                .enabled(false)  // Deshabilitado hasta verificar email
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Crear token de verificación
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .createdAt(LocalDateTime.now())
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        // Enviar email de verificación
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
                token
        );

        log.info("Usuario registrado (pendiente verificación): {}", savedUser.getUsername());

        // NO generar token JWT aquí, usuario debe verificar email primero
        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);

        return AuthResponseDTO.builder()
                .token(null)  // No hay token hasta verificar
                .user(userDTO)
                .build();
    }

    /**
     * Verifica el email del usuario con el token.
     */
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verificando token: {}", token);

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Token de verificación inválido"));

        if (verificationToken.getUsed()) {
            throw new InvalidVerificationTokenException("Este token ya fue utilizado");
        }

        User user = verificationToken.getUser();

        // Activar usuario
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        // Marcar token como usado
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        // Enviar email de bienvenida
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        log.info("Email verificado exitosamente para usuario: {}", user.getUsername());
    }

    /**
     * Reenvía el email de verificación.
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Reenviando email de verificación a: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        if (user.getEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Este email ya está verificado");
        }

        // Eliminar token anterior si existe
        verificationTokenRepository.findByUser(user)
                .ifPresent(verificationTokenRepository::delete);

        // Crear nuevo token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        // Reenviar email
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);

        log.info("Email de verificación reenviado a: {}", email);
    }

    /**
     * Autentica un usuario y genera un token.
     * SOLO permite login si el email está verificado.
     */
    @Transactional
    public AuthResponseDTO login(UserLoginDTO loginDTO) {
        log.info("Intento de login: {}", loginDTO.getUsernameOrEmail());

        // Buscar usuario primero
        User user = userRepository.findByUsername(loginDTO.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(loginDTO.getUsernameOrEmail()))
                .orElseThrow(InvalidCredentialsException::new);

        // Verificar si el email está verificado
        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException("Debes verificar tu email antes de iniciar sesión");
        }

        // Autenticar
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsernameOrEmail(),
                            loginDTO.getPassword()
                    )
            );
        } catch (Exception e) {
            log.error("Login fallido para: {}", loginDTO.getUsernameOrEmail());
            throw new InvalidCredentialsException();
        }

        // Actualizar estado a ONLINE
        user.setStatus(User.UserStatus.ONLINE);
        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Login exitoso: {}", user.getUsername());

        // Generar token
        var userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return AuthResponseDTO.builder()
                .token(token)
                .user(userDTO)
                .build();
    }

    /**
     * Obtiene el usuario autenticado actualmente.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
    }

    /**
     * Obtiene el DTO del usuario autenticado.
     */
    public UserDTO getCurrentUserDTO() {
        User user = getCurrentUser();
        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Obtiene un usuario por ID.
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Obtiene un usuario por username.
     */
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Obtiene todos los usuarios excepto el actual.
     */
    public List<UserDTO> getAllUsers() {
        User currentUser = getCurrentUser();
        List<User> users = userRepository.findAllExceptUser(currentUser.getId());

        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Busca usuarios por username.
     */
    public List<UserDTO> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);

        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza la información del usuario actual.
     */
    @Transactional
    public UserDTO updateCurrentUser(UserUpdateDTO updateDTO) {
        User user = getCurrentUser();
        log.info("Actualizando usuario: {}", user.getUsername());

        // Verificar si el email cambió y si ya existe
        if (updateDTO.getEmail() != null &&
                !updateDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new UserAlreadyExistsException("email", updateDTO.getEmail());
        }

        // Actualizar solo los campos no nulos
        mergerMapper.map(updateDTO, user);

        User updatedUser = userRepository.save(user);
        log.info("Usuario actualizado exitosamente: {}", updatedUser.getUsername());

        return modelMapper.map(updatedUser, UserDTO.class);
    }

    /**
     * Actualiza el estado de conexión del usuario actual.
     */
    @Transactional
    public UserDTO updateUserStatus(UserStatusDTO statusDTO) {
        User user = getCurrentUser();
        log.info("Actualizando estado de usuario {} a {}", user.getUsername(), statusDTO.getStatus());

        user.setStatus(statusDTO.getStatus());
        user.setLastSeenAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        return modelMapper.map(updatedUser, UserDTO.class);
    }

    /**
     * Cierra sesión del usuario actual (cambia estado a OFFLINE).
     */
    @Transactional
    public void logout() {
        User user = getCurrentUser();
        log.info("Cerrando sesión de usuario: {}", user.getUsername());

        user.setStatus(User.UserStatus.OFFLINE);
        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Obtiene usuarios online.
     */
    public List<UserDTO> getOnlineUsers() {
        List<User> users = userRepository.findByStatus(User.UserStatus.ONLINE);

        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la entidad User por ID (uso interno).
     */
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }
}