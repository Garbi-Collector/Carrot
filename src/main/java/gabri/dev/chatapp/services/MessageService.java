package gabri.dev.chatapp.services;

import gabri.dev.chatapp.dtos.MessageDTO;
import gabri.dev.chatapp.dtos.MessageEditDTO;
import gabri.dev.chatapp.dtos.MessageSendDTO;
import gabri.dev.chatapp.entities.ChatRoom;
import gabri.dev.chatapp.entities.Message;
import gabri.dev.chatapp.entities.User;
import gabri.dev.chatapp.exceptions.*;
import gabri.dev.chatapp.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar mensajes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    /**
     * Envía un mensaje a una sala de chat.
     */
    @Transactional
    public MessageDTO sendMessage(MessageSendDTO sendDTO) {
        User sender = userService.getCurrentUser();
        ChatRoom chatRoom = chatRoomService.getChatRoomEntityById(sendDTO.getChatRoomId());

        log.info("Usuario {} enviando mensaje a sala {}",
                sender.getUsername(), chatRoom.getName());

        // Verificar que el usuario sea participante
        if (!chatRoom.getParticipants().contains(sender)) {
            throw new UserNotParticipantException(sender.getUsername(), chatRoom.getName());
        }

        // Crear mensaje
        Message message = Message.builder()
                .content(sendDTO.getContent())
                .type(sendDTO.getType())
                .sender(sender)
                .chatRoom(chatRoom)
                .isEdited(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Mensaje enviado con ID: {}", savedMessage.getId());

        return mapToMessageDTO(savedMessage);
    }

    /**
     * Obtiene mensajes de una sala con paginación.
     */
    public Page<MessageDTO> getMessagesByChatRoom(Long chatRoomId, int page, int size) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntityById(chatRoomId);
        User currentUser = userService.getCurrentUser();

        // Verificar que el usuario sea participante
        if (!chatRoom.getParticipants().contains(currentUser)) {
            throw new UserNotParticipantException(currentUser.getUsername(), chatRoom.getName());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<Message> messages = messageRepository.findByChatRoomId(chatRoomId, pageable);

        return messages.map(this::mapToMessageDTO);
    }

    /**
     * Obtiene los últimos N mensajes de una sala.
     */
    public List<MessageDTO> getLastMessages(Long chatRoomId, int limit) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntityById(chatRoomId);
        User currentUser = userService.getCurrentUser();

        // Verificar que el usuario sea participante
        if (!chatRoom.getParticipants().contains(currentUser)) {
            throw new UserNotParticipantException(currentUser.getUsername(), chatRoom.getName());
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<Message> messages = messageRepository.findLastMessagesByChatRoomId(chatRoomId, pageable);

        // Invertir para que el más antiguo esté primero
        List<Message> reversed = messages.stream()
                .sorted((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()))
                .collect(Collectors.toList());

        return reversed.stream()
                .map(this::mapToMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un mensaje por ID.
     */
    public MessageDTO getMessageById(Long id) {
        Message message = getMessageEntityById(id);
        User currentUser = userService.getCurrentUser();

        // Verificar que el usuario sea participante de la sala
        if (!message.getChatRoom().getParticipants().contains(currentUser)) {
            throw new UserNotParticipantException(
                    currentUser.getUsername(),
                    message.getChatRoom().getName()
            );
        }

        return mapToMessageDTO(message);
    }

    /**
     * Edita un mensaje existente.
     */
    @Transactional
    public MessageDTO editMessage(Long id, MessageEditDTO editDTO) {
        Message message = getMessageEntityById(id);
        User currentUser = userService.getCurrentUser();

        log.info("Usuario {} editando mensaje {}", currentUser.getUsername(), id);

        // Verificar que el usuario sea el autor del mensaje
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Solo puedes editar tus propios mensajes");
        }

        // Verificar que no sea un mensaje de sistema
        if (message.getType() != Message.MessageType.CHAT) {
            throw new InvalidOperationException("No se pueden editar mensajes del sistema");
        }

        // Actualizar contenido
        message.setContent(editDTO.getContent());
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());

        Message updatedMessage = messageRepository.save(message);
        log.info("Mensaje editado: {}", updatedMessage.getId());

        return mapToMessageDTO(updatedMessage);
    }

    /**
     * Elimina un mensaje.
     */
    @Transactional
    public void deleteMessage(Long id) {
        Message message = getMessageEntityById(id);
        User currentUser = userService.getCurrentUser();

        log.info("Usuario {} eliminando mensaje {}", currentUser.getUsername(), id);

        // Verificar que el usuario sea el autor del mensaje
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Solo puedes eliminar tus propios mensajes");
        }

        // Verificar que no sea un mensaje de sistema
        if (message.getType() != Message.MessageType.CHAT) {
            throw new InvalidOperationException("No se pueden eliminar mensajes del sistema");
        }

        messageRepository.delete(message);
        log.info("Mensaje eliminado: {}", id);
    }

    /**
     * Busca mensajes por contenido en una sala.
     */
    public List<MessageDTO> searchMessages(Long chatRoomId, String query) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntityById(chatRoomId);
        User currentUser = userService.getCurrentUser();

        // Verificar que el usuario sea participante
        if (!chatRoom.getParticipants().contains(currentUser)) {
            throw new UserNotParticipantException(currentUser.getUsername(), chatRoom.getName());
        }

        List<Message> messages = messageRepository.searchMessagesByContent(chatRoomId, query);

        return messages.stream()
                .map(this::mapToMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el conteo de mensajes en una sala.
     */
    public Long getMessageCount(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntityById(chatRoomId);
        User currentUser = userService.getCurrentUser();

        // Verificar que el usuario sea participante
        if (!chatRoom.getParticipants().contains(currentUser)) {
            throw new UserNotParticipantException(currentUser.getUsername(), chatRoom.getName());
        }

        return messageRepository.countMessagesByChatRoomId(chatRoomId);
    }

    /**
     * Obtiene la entidad Message por ID (uso interno).
     */
    private Message getMessageEntityById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
    }

    /**
     * Mapea Message a MessageDTO.
     */
    private MessageDTO mapToMessageDTO(Message message) {
        MessageDTO dto = modelMapper.map(message, MessageDTO.class);
        dto.setChatRoomId(message.getChatRoom().getId());
        return dto;
    }
}