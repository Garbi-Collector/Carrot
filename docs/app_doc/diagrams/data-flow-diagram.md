# Data Flow Diagrams

## Entity Interaction Flows

Este diagrama ilustra cómo interactúan las entidades en diferentes escenarios del sistema de chat.

```plantuml
@startuml
!include ./diagrams/data-flow.puml
@enduml
```

## Escenarios de Flujo de Datos

### 1. Creación de Sala Privada (PRIVATE)

**Flujo:**
1. Usuario 1 se autentica en el sistema
2. Sistema crea un nuevo `ChatRoom` con `type=PRIVATE`
3. Usuario 1 es agregado automáticamente a `chat_room_participants`
4. Usuario 2 se autentica en el sistema
5. Usuario 2 es agregado a `chat_room_participants`

**Validaciones:**
- ✅ La sala debe tener exactamente 2 participantes
- ✅ El nombre de la sala se genera automáticamente (ej: "user1-user2")
- ✅ `createdBy` puede ser NULL para salas privadas
- ❌ No se pueden agregar más de 2 participantes

**Resultado:**
- Sala privada creada y lista para intercambio de mensajes
- Ambos usuarios pueden ver la sala en su lista

---

### 2. Envío de Mensaje

**Flujo:**
1. Usuario se autentica
2. Sistema valida que el usuario pertenece a la sala
3. Se crea un nuevo registro en `messages`:
   - `sender_id` = ID del usuario
   - `chat_room_id` = ID de la sala
   - `type` = CHAT (por defecto)
   - `content` = Contenido del mensaje
   - `sent_at` = Timestamp actual
4. Mensaje se asocia con la sala (`chatRoom`)
5. Mensaje se asocia con el usuario remitente (`sender`)
6. Sistema notifica a todos los participantes vía WebSocket

**Validaciones:**
- ✅ El remitente debe ser participante de la sala
- ✅ La sala debe existir y estar activa
- ✅ El contenido no debe estar vacío
- ✅ El tipo de mensaje debe ser válido

**Tipos de mensaje soportados:**
- `CHAT`: Mensaje normal de usuario
- `JOIN`: Sistema notifica que un usuario se unió
- `LEAVE`: Sistema notifica que un usuario salió
- `SYSTEM`: Mensaje automático del sistema
- `FILE`: Mensaje con archivo adjunto
- `IMAGE`: Mensaje con imagen

---

### 3. Creación de Sala Grupal (GROUP)

**Flujo:**
1. Usuario creador se autentica
2. Sistema crea un nuevo `ChatRoom`:
   - `type` = GROUP
   - `createdBy` = ID del usuario creador
   - `name` = Nombre personalizado único
3. Usuario creador es agregado automáticamente como participante
4. Creador invita a más usuarios
5. Cada usuario invitado es agregado a `chat_room_participants`
6. Sistema crea mensajes de tipo `JOIN` para cada nuevo participante

**Validaciones:**
- ✅ La sala debe tener al menos 3 participantes
- ✅ El nombre de la sala debe ser único
- ✅ Debe tener un creador identificado (`createdBy`)
- ✅ Se pueden agregar descripción e imagen
- ❌ No se puede crear una sala grupal con menos de 3 usuarios

**Permisos del creador:**
- Editar nombre, descripción e imagen de la sala
- Agregar o remover participantes
- Eliminar la sala

---

### 4. Actualización de Estado de Usuario

**Flujo:**
1. Usuario cambia su estado (ej: ONLINE → AWAY)
2. Sistema actualiza el campo `status` en `users`
3. Si el estado cambia a OFFLINE:
   - Se actualiza `lastSeenAt` con timestamp actual
4. Sistema emite evento WebSocket a todos los contactos del usuario
5. Otros usuarios ven el estado actualizado en tiempo real

**Estados disponibles:**
- `ONLINE`: Usuario activo y disponible
- `OFFLINE`: Usuario desconectado
- `AWAY`: Usuario conectado pero ausente
- `BUSY`: Usuario conectado pero no disponible

**Eventos relacionados:**
- Actualización automática al conectar/desconectar WebSocket
- Actualización manual por parte del usuario
- Timeout automático después de inactividad (configurable)

---

## Reglas de Negocio Implementadas

### Integridad de Salas Privadas

```java
// Validación antes de agregar participantes
if (chatRoom.getType() == ChatRoomType.PRIVATE) {
    if (chatRoom.getParticipants().size() >= 2) {
        throw new BusinessException("Una sala privada solo puede tener 2 participantes");
    }
}
```

### Integridad de Salas Grupales

```java
// Validación al crear sala grupal
if (chatRoom.getType() == ChatRoomType.GROUP) {
    if (participants.size() < 3) {
        throw new BusinessException("Una sala grupal debe tener al menos 3 participantes");
    }
    if (chatRoom.getCreatedBy() == null) {
        throw new BusinessException("Una sala grupal debe tener un creador");
    }
}
```

### Validación de Mensajes

```java
// Validación antes de enviar mensaje
if (!chatRoom.getParticipants().contains(sender)) {
    throw new BusinessException("El usuario no es participante de esta sala");
}

if (message.getContent() == null || message.getContent().trim().isEmpty()) {
    throw new BusinessException("El mensaje no puede estar vacío");
}
```

### Actualización de Timestamps

```java
// Al editar un mensaje
message.setEditedAt(LocalDateTime.now());
message.setIsEdited(true);

// Al desconectar usuario
user.setStatus(UserStatus.OFFLINE);
user.setLastSeenAt(LocalDateTime.now());
```

---

## Cascadas y Eliminaciones

### Eliminación de Usuario

Cuando se elimina un usuario (`DELETE FROM users WHERE id = ?`):

1. **Cascada en `messages`:**
   - ✅ Se eliminan todos los mensajes enviados por el usuario
   - Configurado con `orphanRemoval = true`

2. **Cascada en `chat_room_participants`:**
   - ✅ Se eliminan todas las referencias en salas
   - Puede dejar salas sin participantes (requiere limpieza)

3. **Salas creadas (`chat_rooms.created_by`):**
   - ⚠️ Depende de la estrategia configurada
   - Opción 1: SET NULL (sala queda sin creador)
   - Opción 2: CASCADE (se elimina la sala)

### Eliminación de Sala

Cuando se elimina una sala (`DELETE FROM chat_rooms WHERE id = ?`):

1. **Cascada en `messages`:**
   - ✅ Se eliminan todos los mensajes de la sala
   - Configurado con `orphanRemoval = true`

2. **Cascada en `chat_room_participants`:**
   - ✅ Se eliminan todas las relaciones con usuarios
   - Los usuarios permanecen en el sistema

---

## Optimizaciones de Consulta

### Fetch Lazy

```java
// Entidades relacionadas se cargan bajo demanda
@ManyToOne(fetch = FetchType.LAZY)
private User sender;

@ManyToOne(fetch = FetchType.LAZY)
private ChatRoom chatRoom;
```

**Ventajas:**
- Reduce queries innecesarias
- Mejora rendimiento en listas grandes
- Solo carga datos cuando se necesitan

**Uso correcto:**
```java
// Cargar explícitamente cuando sea necesario
Message message = messageRepository.findById(id)
    .orElseThrow();
message.getSender().getUsername(); // Trigger lazy load
```

### Índices Recomendados

```sql
-- Índices para búsquedas frecuentes
CREATE INDEX idx_messages_chat_room_sent_at 
    ON messages(chat_room_id, sent_at DESC);

CREATE INDEX idx_messages_sender 
    ON messages(sender_id);

CREATE INDEX idx_users_status 
    ON users(status) 
    WHERE status = 'ONLINE';

CREATE INDEX idx_chat_room_participants_user 
    ON chat_room_participants(user_id);
```

**Beneficios:**
- Queries más rápidas para obtener mensajes recientes
- Búsqueda eficiente de usuarios online
- Joins optimizados en relaciones Many-to-Many
