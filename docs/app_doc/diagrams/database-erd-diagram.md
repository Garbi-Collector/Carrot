# Database Schema - Entity Relationship Diagram

## Database Tables and Relationships

Este diagrama muestra el esquema de base de datos real con nombres de tablas, columnas y constraints.

```plantuml
@startuml
!include ./diagrams/database-erd.puml
@enduml
```

## Estructura de Tablas

### users

Tabla principal de usuarios del sistema.

**Columnas:**
- `id` (BIGINT, PK): Identificador único auto-incremental
- `username` (VARCHAR(50), UNIQUE, NOT NULL): Nombre de usuario
- `email` (VARCHAR(100), UNIQUE, NOT NULL): Correo electrónico
- `password` (VARCHAR(255), NOT NULL): Contraseña encriptada (BCrypt)
- `full_name` (VARCHAR(100)): Nombre completo opcional
- `avatar_url` (VARCHAR(500)): URL de imagen de perfil
- `status` (VARCHAR(20), NOT NULL): Estado de conexión (ONLINE|OFFLINE|AWAY|BUSY)
- `enabled` (BOOLEAN, NOT NULL, DEFAULT true): Cuenta activa
- `created_at` (TIMESTAMP, NOT NULL): Fecha de registro
- `updated_at` (TIMESTAMP): Última actualización
- `last_seen_at` (TIMESTAMP): Última conexión

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE INDEX (`username`)
- UNIQUE INDEX (`email`)

**Valores por defecto:**
- `status` = 'OFFLINE'
- `enabled` = true

---

### messages

Tabla de mensajes enviados en las salas de chat.

**Columnas:**
- `id` (BIGINT, PK): Identificador único auto-incremental
- `sender_id` (BIGINT, FK, NOT NULL): Referencia a `users.id`
- `chat_room_id` (BIGINT, FK, NOT NULL): Referencia a `chat_rooms.id`
- `content` (TEXT, NOT NULL): Contenido del mensaje
- `type` (VARCHAR(20), NOT NULL): Tipo de mensaje (CHAT|JOIN|LEAVE|SYSTEM|FILE|IMAGE)
- `sent_at` (TIMESTAMP, NOT NULL): Fecha y hora de envío
- `edited_at` (TIMESTAMP): Fecha y hora de última edición
- `is_edited` (BOOLEAN, NOT NULL, DEFAULT false): Indica si fue editado

**Índices:**
- PRIMARY KEY (`id`)
- FOREIGN KEY (`sender_id`) REFERENCES `users(id)`
- FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms(id)`
- INDEX (`sent_at`) para ordenamiento de mensajes

**Valores por defecto:**
- `type` = 'CHAT'
- `is_edited` = false

**Cascadas:**
- ON DELETE CASCADE (cuando se elimina el usuario o la sala)

---

### chat_rooms

Tabla de salas de chat (privadas o grupales).

**Columnas:**
- `id` (BIGINT, PK): Identificador único auto-incremental
- `name` (VARCHAR(100), UNIQUE, NOT NULL): Nombre de la sala
- `type` (VARCHAR(20), NOT NULL): Tipo de sala (PRIVATE|GROUP)
- `description` (VARCHAR(500)): Descripción opcional
- `image_url` (VARCHAR(500)): URL de imagen de la sala
- `created_by_user_id` (BIGINT, FK): Referencia a `users.id` (creador)
- `created_at` (TIMESTAMP, NOT NULL): Fecha de creación
- `updated_at` (TIMESTAMP): Última actualización

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE INDEX (`name`)
- FOREIGN KEY (`created_by_user_id`) REFERENCES `users(id)`

**Valores por defecto:**
- `type` = 'PRIVATE'

---

### chat_room_participants

Tabla intermedia para la relación Many-to-Many entre usuarios y salas.

**Columnas:**
- `chat_room_id` (BIGINT, FK, PK): Referencia a `chat_rooms.id`
- `user_id` (BIGINT, FK, PK): Referencia a `users.id`

**Índices:**
- PRIMARY KEY (`chat_room_id`, `user_id`): Clave primaria compuesta
- FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms(id)`
- FOREIGN KEY (`user_id`) REFERENCES `users(id)`
- INDEX (`chat_room_id`)
- INDEX (`user_id`)

**Reglas:**
- Evita duplicados automáticamente por la PK compuesta
- Permite queries eficientes en ambas direcciones

---

## Relaciones entre Tablas

### 1:N - users → messages
- Un usuario puede enviar múltiples mensajes
- Cada mensaje pertenece a un único usuario
- **FK:** `messages.sender_id` → `users.id`
- **Cascada:** Si se elimina un usuario, se eliminan todos sus mensajes

### 1:N - chat_rooms → messages
- Una sala puede contener múltiples mensajes
- Cada mensaje pertenece a una única sala
- **FK:** `messages.chat_room_id` → `chat_rooms.id`
- **Cascada:** Si se elimina una sala, se eliminan todos sus mensajes

### 1:N - users → chat_rooms (creator)
- Un usuario puede crear múltiples salas grupales
- Cada sala grupal tiene un único creador (puede ser NULL para salas privadas)
- **FK:** `chat_rooms.created_by_user_id` → `users.id`

### N:M - users ↔ chat_rooms (participants)
- Un usuario puede participar en múltiples salas
- Una sala puede tener múltiples usuarios participantes
- **Tabla intermedia:** `chat_room_participants`
- **FKs:** 
  - `chat_room_participants.user_id` → `users.id`
  - `chat_room_participants.chat_room_id` → `chat_rooms.id`

---

## Constraints y Validaciones

### Nivel de Base de Datos

1. **Uniqueness:**
   - `users.username` debe ser único
   - `users.email` debe ser único
   - `chat_rooms.name` debe ser único

2. **Not Null:**
   - Campos críticos marcados como NOT NULL
   - Evita inconsistencias en datos esenciales

3. **Foreign Keys:**
   - Garantizan integridad referencial
   - Evitan registros huérfanos

### Nivel de Aplicación

1. **ChatRoom Type Validation:**
   - PRIVATE: Exactamente 2 participantes
   - GROUP: 3 o más participantes

2. **Password Security:**
   - Almacenada con BCrypt
   - Nunca retornada en responses de API

3. **Message Editing:**
   - Actualizar `edited_at` al editar
   - Cambiar `is_edited` a true

---

## Queries Comunes

### Obtener mensajes de una sala ordenados por fecha
```sql
SELECT * FROM messages 
WHERE chat_room_id = ? 
ORDER BY sent_at DESC 
LIMIT 50;
```

### Obtener salas de un usuario con último mensaje
```sql
SELECT cr.*, m.content as last_message, m.sent_at as last_message_at
FROM chat_rooms cr
JOIN chat_room_participants crp ON cr.id = crp.chat_room_id
LEFT JOIN messages m ON m.id = (
    SELECT id FROM messages 
    WHERE chat_room_id = cr.id 
    ORDER BY sent_at DESC 
    LIMIT 1
)
WHERE crp.user_id = ?
ORDER BY m.sent_at DESC;
```

### Obtener usuarios conectados
```sql
SELECT * FROM users 
WHERE status = 'ONLINE' 
AND enabled = true;
```
