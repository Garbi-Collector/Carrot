package gabri.dev.chatapp.repositories;

import gabri.dev.chatapp.entities.ChatRoom;
import gabri.dev.chatapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ChatRoom.
 * Proporciona métodos para acceder y manipular salas de chat.
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * Busca una sala de chat por su nombre.
     * @param name el nombre de la sala
     * @return Optional con la sala si existe
     */
    Optional<ChatRoom> findByName(String name);

    /**
     * Verifica si existe una sala con el nombre dado.
     * @param name el nombre de la sala
     * @return true si existe, false en caso contrario
     */
    Boolean existsByName(String name);

    /**
     * Busca salas de chat por tipo.
     * @param type el tipo de sala (PRIVATE o GROUP)
     * @return lista de salas de ese tipo
     */
    List<ChatRoom> findByType(ChatRoom.ChatRoomType type);

    /**
     * Busca todas las salas de chat donde participa un usuario.
     * @param user el usuario
     * @return lista de salas donde participa
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p = :user ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findByParticipantsContaining(@Param("user") User user);

    /**
     * Busca salas de chat por ID de usuario participante.
     * @param userId el ID del usuario
     * @return lista de salas donde participa
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p.id = :userId ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findByParticipantId(@Param("userId") Long userId);

    /**
     * Busca una sala de chat privada entre dos usuarios específicos.
     * Una sala privada debe tener exactamente 2 participantes.
     * @param userId1 el ID del primer usuario
     * @param userId2 el ID del segundo usuario
     * @return Optional con la sala si existe
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.type = 'PRIVATE' " +
            "AND :user1 MEMBER OF cr.participants " +
            "AND :user2 MEMBER OF cr.participants " +
            "AND SIZE(cr.participants) = 2")
    Optional<ChatRoom> findPrivateChatRoom(@Param("user1") User user1,
                                           @Param("user2") User user2);

    /**
     * Busca salas grupales creadas por un usuario específico.
     * @param userId el ID del usuario creador
     * @return lista de salas creadas por el usuario
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.createdBy.id = :userId AND cr.type = 'GROUP'")
    List<ChatRoom> findGroupChatRoomsCreatedBy(@Param("userId") Long userId);

    /**
     * Busca salas cuyo nombre contenga el texto dado (case insensitive).
     * @param name el texto a buscar
     * @return lista de salas que coinciden
     */
    List<ChatRoom> findByNameContainingIgnoreCase(String name);

    /**
     * Cuenta el número de participantes en una sala.
     * @param chatRoomId el ID de la sala
     * @return número de participantes
     */
    @Query("SELECT SIZE(cr.participants) FROM ChatRoom cr WHERE cr.id = :chatRoomId")
    Integer countParticipants(@Param("chatRoomId") Long chatRoomId);

    /**
     * Verifica si un usuario es participante de una sala.
     * @param chatRoomId el ID de la sala
     * @param userId el ID del usuario
     * @return true si es participante, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END " +
            "FROM ChatRoom cr JOIN cr.participants p " +
            "WHERE cr.id = :chatRoomId AND p.id = :userId")
    Boolean isUserParticipant(@Param("chatRoomId") Long chatRoomId,
                              @Param("userId") Long userId);
}