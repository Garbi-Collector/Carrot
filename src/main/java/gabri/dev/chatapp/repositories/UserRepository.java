package gabri.dev.chatapp.repositories;

import gabri.dev.chatapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * Proporciona métodos para acceder y manipular usuarios en la base de datos.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su username.
     * @param username el nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email.
     * @param email el correo electrónico
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el username dado.
     * @param username el nombre de usuario
     * @return true si existe, false en caso contrario
     */
    Boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado.
     * @param email el correo electrónico
     * @return true si existe, false en caso contrario
     */
    Boolean existsByEmail(String email);

    /**
     * Busca usuarios por su estado de conexión.
     * @param status el estado del usuario
     * @return lista de usuarios con ese estado
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * Busca usuarios cuyo username contenga el texto dado (case insensitive).
     * @param username el texto a buscar
     * @return lista de usuarios que coinciden
     */
    List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Busca usuarios habilitados.
     * @param enabled true para buscar habilitados, false para deshabilitados
     * @return lista de usuarios
     */
    List<User> findByEnabled(Boolean enabled);

    /**
     * Actualiza el estado de un usuario.
     * @param userId el ID del usuario
     * @param status el nuevo estado
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.lastSeenAt = :lastSeen WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") Long userId,
                          @Param("status") User.UserStatus status,
                          @Param("lastSeen") LocalDateTime lastSeen);

    /**
     * Busca todos los usuarios excepto el usuario dado.
     * Útil para listar usuarios con los que se puede iniciar un chat.
     * @param userId el ID del usuario a excluir
     * @return lista de usuarios
     */
    @Query("SELECT u FROM User u WHERE u.id != :userId AND u.enabled = true")
    List<User> findAllExceptUser(@Param("userId") Long userId);

    /**
     * Busca usuarios que no están en una sala de chat específica.
     * @param chatRoomId el ID de la sala de chat
     * @return lista de usuarios
     */
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT p.id FROM ChatRoom cr JOIN cr.participants p WHERE cr.id = :chatRoomId)")
    List<User> findUsersNotInChatRoom(@Param("chatRoomId") Long chatRoomId);
}