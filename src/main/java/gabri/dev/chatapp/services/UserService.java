package gabri.dev.chatapp.services;

import gabri.dev.chatapp.dtos.*;
import gabri.dev.chatapp.entities.User;
import gabri.dev.chatapp.exceptions.*;
import gabri.dev.chatapp.repositories.UserRepository;
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
import java.util.stream.Collectors;

/**
 * Servicio para gestionar usuarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Qualifier("mergerMapper")
    private final ModelMapper mergerMapper;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Registra un nuevo usuario.
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

        // Crear el usuario
        User user = User.builder()
                .username(registrationDTO.getUsername())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .fullName(registrationDTO.getFullName())
                .status(User.UserStatus.OFFLINE)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {}", savedUser.getUsername());

        // Generar token JWT
        var userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String token = jwtService.generateToken(userDetails);

        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);

        return AuthResponseDTO.builder()
                .token(token)
                .user(userDTO)
                .build();
    }

    /**
     * Autentica un usuario y genera un token.
     */
    @Transactional
    public AuthResponseDTO login(UserLoginDTO loginDTO) {
        log.info("Intento de login: {}", loginDTO.getUsernameOrEmail());

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

        // Buscar usuario
        User user = userRepository.findByUsername(loginDTO.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(loginDTO.getUsernameOrEmail()))
                .orElseThrow(InvalidCredentialsException::new);

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