package gabri.dev.chatapp.services;

import gabri.dev.chatapp.dtos.*;
import gabri.dev.chatapp.entities.ChatRoom;
import gabri.dev.chatapp.entities.Message;
import gabri.dev.chatapp.entities.User;
import gabri.dev.chatapp.exceptions.*;
import gabri.dev.chatapp.repositories.ChatRoomRepository;
import gabri.dev.chatapp.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar salas de chat.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Qualifier("mergerMapper")
    private final ModelMapper mergerMapper;

    /**
     * Crea una nueva sala de chat grupal.
     */
    @Transactional
    public ChatRoomDTO createGroupChatRoom(ChatRoomCreateDTO createDTO) {
        User creator = userService.getCurrentUser();
        log.info("Creando sala de chat grupal: {} por usuario: {}",
                createDTO.getName(), creator.getUsername());

        // Verificar que el nombre no exista
        if (chatRoomRepository.existsByName(createDTO.getName())) {
            throw new ChatRoomAlreadyExistsException(createDTO.getName());
        }

        // Crear la sala
        ChatRoom chatRoom = ChatRoom.builder()
                .name(createDTO.getName())
                .type(ChatRoom.ChatRoomType.GROUP)
                .description(createDTO.getDescription())
                .imageUrl(createDTO.getImageUrl())
                .createdBy(creator)
                .participants(new HashSet<>())
                .build();

        // Agregar el creador como participante
        chatRoom.getParticipants().add(creator);

        // Agregar otros participantes si se especificaron
        if (createDTO.getParticipantIds() != null && !createDTO.getParticipantIds().isEmpty()) {
            for (Long userId : createDTO.getParticipantIds()) {
                User participant = userService.getUserEntityById(userId);
                chatRoom.getParticipants().add(participant);
            }
        }

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        log.info("Sala de chat grupal creada: {}", savedChatRoom.getName());

        // Crear mensaje de sistema
        createSystemMessage(savedChatRoom, creator.getUsername() + " creó el grupo", Message.MessageType.SYSTEM);

        return mapToChatRoomDTO(savedChatRoom);
    }

    /**
     * Crea o recupera una sala de chat privada entre dos usuarios.
     */
    @Transactional
    public ChatRoomDTO createOrGetPrivateChatRoom(PrivateChatRoomCreateDTO createDTO) {
        User currentUser = userService.getCurrentUser();
        User recipient = userService.getUserEntityById(createDTO.getRecipientId());

        log.info("Creando/recuperando chat privado entre {} y {}",
                currentUser.getUsername(), recipient.getUsername());

        // Verificar que no sea consigo mismo
        if (currentUser.getId().equals(recipient.getId())) {
            throw new InvalidOperationException("No puedes crear un chat privado contigo mismo");
        }

        // Buscar si ya existe una sala privada entre estos usuarios
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findPrivateChatRoom(currentUser, recipient);

        if (existingRoom.isPresent()) {
            log.info("Chat privado ya existe: {}", existingRoom.get().getName());
            return mapToChatRoomDTO(existingRoom.get());
        }

        // Crear nueva sala privada
        String roomName = "private_" + Math.min(currentUser.getId(), recipient.getId())
                + "_" + Math.max(currentUser.getId(), recipient.getId());

        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .type(ChatRoom.ChatRoomType.PRIVATE)
                .participants(new HashSet<>(Arrays.asList(currentUser, recipient)))
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        log.info("Chat privado creado: {}", savedChatRoom.getName());

        return mapToChatRoomDTO(savedChatRoom);
    }

    /**
     * Obtiene todas las salas de chat del usuario actual.
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getCurrentUserChatRooms() {
        User currentUser = userService.getCurrentUser();
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantId(currentUser.getId());

        return chatRooms.stream()
                .map(this::mapToChatRoomDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una sala de chat por ID.
     */
    @Transactional(readOnly = true)
    public ChatRoomDTO getChatRoomById(Long id) {
        ChatRoom chatRoom = getChatRoomEntityById(id);
        verifyUserIsParticipant(chatRoom);

        return mapToChatRoomDTO(chatRoom);
    }

    /**
     * Actualiza una sala de chat grupal.
     */
    @Transactional
    public ChatRoomDTO updateChatRoom(Long id, ChatRoomUpdateDTO updateDTO) {
        ChatRoom chatRoom = getChatRoomEntityById(id);
        User currentUser = userService.getCurrentUser();

        log.info("Actualizando sala de chat: {} por usuario: {}",
                chatRoom.getName(), currentUser.getUsername());

        // Verificar que sea sala grupal
        if (chatRoom.getType() == ChatRoom.ChatRoomType.PRIVATE) {
            throw new InvalidOperationException("No se puede editar una sala de chat privada");
        }

        // Verificar que el usuario sea el creador
        if (!chatRoom.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Solo el creador puede editar la sala");
        }

        // Verificar si el nombre cambió y si ya existe
        if (updateDTO.getName() != null &&
                !updateDTO.getName().equals(chatRoom.getName()) &&
                chatRoomRepository.existsByName(updateDTO.getName())) {
            throw new ChatRoomAlreadyExistsException(updateDTO.getName());
        }

        // Actualizar solo campos no nulos
        mergerMapper.map(updateDTO, chatRoom);

        ChatRoom updatedChatRoom = chatRoomRepository.save(chatRoom);
        log.info("Sala de chat actualizada: {}", updatedChatRoom.getName());

        return mapToChatRoomDTO(updatedChatRoom);
    }

    /**
     * Agrega un participante a una sala de chat grupal.
     */
    @Transactional
    public ChatRoomDTO addParticipant(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoomEntityById(chatRoomId);
        User currentUser = userService.getCurrentUser();
        User newParticipant = userService.getUserEntityById(userId);

        log.info("Agregando participante {} a sala {}",
                newParticipant.getUsername(), chatRoom.getName());

        // Verificar que sea sala grupal
        if (chatRoom.getType() == ChatRoom.ChatRoomType.PRIVATE) {
            throw new InvalidOperationException("No se pueden agregar participantes a chats privados");
        }

        // Verificar que el usuario actual sea participante
        verifyUserIsParticipant(chatRoom);

        // Verificar que el nuevo usuario no sea ya participante
        if (chatRoom.getParticipants().contains(newParticipant)) {
            throw new InvalidOperationException("El usuario ya es participante de la sala");
        }

        // Agregar participante
        chatRoom.getParticipants().add(newParticipant);
        ChatRoom updatedChatRoom = chatRoomRepository.save(chatRoom);

        // Crear mensaje de sistema
        createSystemMessage(chatRoom,
                newParticipant.getUsername() + " se unió al grupo",
                Message.MessageType.JOIN);

        log.info("Participante agregado exitosamente");

        return mapToChatRoomDTO(updatedChatRoom);
    }

    /**
     * Remueve un participante de una sala de chat grupal.
     */
    @Transactional
    public ChatRoomDTO removeParticipant(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoomEntityById(chatRoomId);
        User currentUser = userService.getCurrentUser();
        User participantToRemove = userService.getUserEntityById(userId);

        log.info("Removiendo participante {} de sala {}",
                participantToRemove.getUsername(), chatRoom.getName());

        // Verificar que sea sala grupal
        if (chatRoom.getType() == ChatRoom.ChatRoomType.PRIVATE) {
            throw new InvalidOperationException("No se pueden remover participantes de chats privados");
        }

        // Solo el creador puede remover a otros, cualquiera puede salirse
        if (!currentUser.getId().equals(userId) &&
                !chatRoom.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Solo el creador puede remover participantes");
        }

        // Verificar que el usuario sea participante
        if (!chatRoom.getParticipants().contains(participantToRemove)) {
            throw new InvalidOperationException("El usuario no es participante de la sala");
        }

        // Remover participante
        chatRoom.getParticipants().remove(participantToRemove);
        ChatRoom updatedChatRoom = chatRoomRepository.save(chatRoom);

        // Crear mensaje de sistema
        createSystemMessage(chatRoom,
                participantToRemove.getUsername() + " abandonó el grupo",
                Message.MessageType.LEAVE);

        log.info("Participante removido exitosamente");

        return mapToChatRoomDTO(updatedChatRoom);
    }

    /**
     * Abandona una sala de chat.
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId) {
        User currentUser = userService.getCurrentUser();
        removeParticipant(chatRoomId, currentUser.getId());
    }

    /**
     * Elimina una sala de chat (solo el creador en salas grupales).
     */
    @Transactional
    public void deleteChatRoom(Long id) {
        ChatRoom chatRoom = getChatRoomEntityById(id);
        User currentUser = userService.getCurrentUser();

        log.info("Eliminando sala de chat: {} por usuario: {}",
                chatRoom.getName(), currentUser.getUsername());

        // Verificar que sea sala grupal
        if (chatRoom.getType() == ChatRoom.ChatRoomType.PRIVATE) {
            throw new InvalidOperationException("No se pueden eliminar chats privados");
        }

        // Verificar que sea el creador
        if (!chatRoom.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Solo el creador puede eliminar la sala");
        }

        chatRoomRepository.delete(chatRoom);
        log.info("Sala de chat eliminada: {}", chatRoom.getName());
    }

    /**
     * Busca salas de chat por nombre.
     */
    public List<ChatRoomDTO> searchChatRooms(String query) {
        User currentUser = userService.getCurrentUser();
        List<ChatRoom> chatRooms = chatRoomRepository.findByNameContainingIgnoreCase(query);

        // Filtrar solo las salas donde el usuario es participante
        return chatRooms.stream()
                .filter(room -> room.getParticipants().contains(currentUser))
                .map(this::mapToChatRoomDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la entidad ChatRoom por ID (uso interno).
     */
    @Transactional(readOnly = true)
    public ChatRoom getChatRoomEntityById(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", id));
    }

    /**
     * Verifica que el usuario actual sea participante de la sala.
     */
    private void verifyUserIsParticipant(ChatRoom chatRoom) {
        User currentUser = userService.getCurrentUser();

        if (!chatRoom.getParticipants().contains(currentUser)) {
            throw new UserNotParticipantException(
                    currentUser.getUsername(),
                    chatRoom.getName()
            );
        }
    }

    /**
     * Crea un mensaje de sistema en una sala.
     */
    private void createSystemMessage(ChatRoom chatRoom, String content, Message.MessageType type) {
        Message message = Message.builder()
                .content(content)
                .type(type)
                .chatRoom(chatRoom)
                .sender(chatRoom.getCreatedBy()) // El creador como sender de mensajes del sistema
                .build();

        messageRepository.save(message);
    }

    /**
     * Mapea ChatRoom a ChatRoomDTO incluyendo información adicional.
     */
    private ChatRoomDTO mapToChatRoomDTO(ChatRoom chatRoom) {
        ChatRoomDTO dto = modelMapper.map(chatRoom, ChatRoomDTO.class);

        // Mapear participantes
        List<UserDTO> participants = chatRoom.getParticipants().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
        dto.setParticipants(participants);

        // Mapear creador
        if (chatRoom.getCreatedBy() != null) {
            dto.setCreatedBy(modelMapper.map(chatRoom.getCreatedBy(), UserDTO.class));
        }

        // Obtener último mensaje
        Message lastMessage = messageRepository.findLastMessageByChatRoomId(chatRoom.getId());
        if (lastMessage != null) {
            dto.setLastMessage(modelMapper.map(lastMessage, MessageDTO.class));
        }

        // Número de participantes
        dto.setParticipantCount(chatRoom.getParticipants().size());

        // TODO: Implementar conteo de mensajes no leídos en el futuro
        dto.setUnreadCount(0L);

        return dto;
    }
}