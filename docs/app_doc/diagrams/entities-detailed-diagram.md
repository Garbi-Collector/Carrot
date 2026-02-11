# Entity Class Diagrams - Detailed View

## Detailed Entity Attributes and Relationships

Este diagrama muestra los atributos detallados de cada entidad JPA, incluyendo tipos de datos, constraints y relaciones.

```plantuml
@startuml
!include ./diagrams/entities-detailed.puml
@enduml
```

## Descripción de Atributos

### User

| Atributo | Tipo | Constraints | Descripción |
|----------|------|-------------|-------------|
| id | Long | PK, Auto-increment | Identificador único |
| username | String(50) | Unique, Not Null | Nombre de usuario |
| email | String(100) | Unique, Not Null | Correo electrónico |
| password | String | Not Null | Contraseña encriptada |
| fullName | String(100) | Nullable | Nombre completo |
| avatarUrl | String(500) | Nullable | URL de imagen de perfil |
| status | UserStatus | Not Null | Estado de conexión (default: OFFLINE) |
| enabled | Boolean | Not Null | Cuenta habilitada (default: true) |
| createdAt | LocalDateTime | Not Null, Immutable | Fecha de creación |
| updatedAt | LocalDateTime | Auto-update | Fecha de última actualización |
| lastSeenAt | LocalDateTime | Nullable | Última conexión |

### Message

| Atributo | Tipo | Constraints | Descripción |
|----------|------|-------------|-------------|
| id | Long | PK, Auto-increment | Identificador único |
| content | String | Not Null, TEXT | Contenido del mensaje |
| type | MessageType | Not Null | Tipo de mensaje (default: CHAT) |
| sentAt | LocalDateTime | Not Null, Immutable | Fecha de envío |
| editedAt | LocalDateTime | Nullable | Fecha de última edición |
| isEdited | Boolean | Not Null | Indica si fue editado (default: false) |
| sender | User | FK, Not Null | Remitente del mensaje |
| chatRoom | ChatRoom | FK, Not Null | Sala donde se envió |

### ChatRoom

| Atributo | Tipo | Constraints | Descripción |
|----------|------|-------------|-------------|
| id | Long | PK, Auto-increment | Identificador único |
| name | String(100) | Unique, Not Null | Nombre de la sala |
| type | ChatRoomType | Not Null | Tipo de sala (default: PRIVATE) |
| description | String(500) | Nullable | Descripción de la sala |
| imageUrl | String(500) | Nullable | URL de imagen de la sala |
| createdAt | LocalDateTime | Not Null, Immutable | Fecha de creación |
| updatedAt | LocalDateTime | Auto-update | Fecha de última actualización |
| participants | Set\<User\> | Many-to-Many | Usuarios participantes |
| messages | Set\<Message\> | One-to-Many | Mensajes de la sala |
| createdBy | User | FK, Nullable | Creador de la sala |

## Cardinalidades

### User ↔ Message
- **1 User** puede enviar **muchos Messages** (0..*)
- **1 Message** pertenece a **1 User** (sender)

### User ↔ ChatRoom (participants)
- **1 User** puede participar en **muchas ChatRooms** (0..*)
- **1 ChatRoom** puede tener **muchos Users** como participantes (0..*)
- **Tabla intermedia:** `chat_room_participants`

### User ↔ ChatRoom (creator)
- **1 User** puede crear **muchas ChatRooms** (0..*)
- **1 ChatRoom** es creada por **0..1 User** (puede ser null para salas privadas auto-generadas)

### ChatRoom ↔ Message
- **1 ChatRoom** contiene **muchos Messages** (0..*)
- **1 Message** pertenece a **1 ChatRoom**

## Cascade Operations

### User
- **sentMessages**: CASCADE ALL, ORPHAN REMOVAL
  - Si se elimina un User, se eliminan todos sus mensajes

### ChatRoom
- **messages**: CASCADE ALL, ORPHAN REMOVAL
  - Si se elimina un ChatRoom, se eliminan todos sus mensajes

## Fetch Strategies

- **Message.sender**: LAZY
- **Message.chatRoom**: LAZY
- Optimización de consultas mediante lazy loading
