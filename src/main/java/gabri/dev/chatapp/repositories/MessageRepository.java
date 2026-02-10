package gabri.dev.chatapp.repositories;

import gabri.dev.chatapp.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Message.
 * Proporciona métodos para acceder y manipular mensajes.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Busca todos los mensajes de una sala de chat ordenados por fecha.
     * @param chatRoomId el ID de la sala
     * @return lista de mensajes ordenados del más antiguo al más reciente
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt ASC")
    List<Message> findByChatRoomIdOrderBySentAtAsc(@Param("chatRoomId") Long chatRoomId);

    /**
     * Busca mensajes de una sala con paginación.
     * @param chatRoomId el ID de la sala
     * @param pageable información de paginación
     * @return página de mensajes
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt DESC")
    Page<Message> findByChatRoomId(@Param("chatRoomId") Long chatRoomId, Pageable pageable);

    /**
     * Busca los últimos N mensajes de una sala.
     * @param chatRoomId el ID de la sala
     * @paramlimit número de mensajes a recuperar
     * @return lista de mensajes
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt DESC")
    List<Message> findLastMessagesByChatRoomId(@Param("chatRoomId") Long chatRoomId,
                                               Pageable pageable);

    /**
     * Busca mensajes enviados por un usuario específico.
     * @param senderId el ID del usuario
     * @return lista de mensajes
     */
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId ORDER BY m.sentAt DESC")
    List<Message> findBySenderId(@Param("senderId") Long senderId);

    /**
     * Busca mensajes por tipo en una sala específica.
     * @param chatRoomId el ID de la sala
     * @param type el tipo de mensaje
     * @return lista de mensajes
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId AND m.type = :type ORDER BY m.sentAt ASC")
    List<Message> findByChatRoomIdAndType(@Param("chatRoomId") Long chatRoomId,
                                          @Param("type") Message.MessageType type);

    /**
     * Busca el último mensaje de una sala de chat.
     * @param chatRoomId el ID de la sala
     * @return el último mensaje o null
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt DESC LIMIT 1")
    Message findLastMessageByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * Cuenta los mensajes en una sala de chat.
     * @param chatRoomId el ID de la sala
     * @return número de mensajes
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoom.id = :chatRoomId")
    Long countMessagesByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * Busca mensajes en un rango de fechas.
     * @param chatRoomId el ID de la sala
     * @param startDate fecha de inicio
     * @param endDate fecha de fin
     * @return lista de mensajes
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId " +
            "AND m.sentAt BETWEEN :startDate AND :endDate ORDER BY m.sentAt ASC")
    List<Message> findMessagesBetweenDates(@Param("chatRoomId") Long chatRoomId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Busca mensajes que contengan un texto específico (búsqueda).
     * @param chatRoomId el ID de la sala
     * @param searchTerm el texto a buscar
     * @return lista de mensajes que coinciden
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY m.sentAt DESC")
    List<Message> searchMessagesByContent(@Param("chatRoomId") Long chatRoomId,
                                          @Param("searchTerm") String searchTerm);

    /**
     * Elimina todos los mensajes de una sala de chat.
     * @param chatRoomId el ID de la sala
     */
    @Query("DELETE FROM Message m WHERE m.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * Busca mensajes editados en una sala.
     * @param chatRoomId el ID de la sala
     * @return lista de mensajes editados
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId AND m.isEdited = true ORDER BY m.editedAt DESC")
    List<Message> findEditedMessages(@Param("chatRoomId") Long chatRoomId);
}