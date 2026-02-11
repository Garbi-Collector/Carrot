# Entity Diagrams

## Database Entities Overview

Este diagrama muestra las entidades JPA principales del sistema de chat y sus relaciones.

```plantuml
@startuml
!include ./diagrams/entities.puml
@enduml
```

### Entidades Principales

#### User
Representa un usuario en el sistema de chat. Almacena información de autenticación, perfil y estado de conexión.

**Relaciones:**
- One-to-Many con `Message` (mensajes enviados)
- Many-to-Many con `ChatRoom` (salas de chat en las que participa)
- One-to-Many con `ChatRoom` (salas creadas)

**Estados posibles (UserStatus):**
- `ONLINE`: Usuario conectado y activo
- `OFFLINE`: Usuario desconectado
- `AWAY`: Usuario conectado pero ausente
- `BUSY`: Usuario conectado pero ocupado

#### Message
Representa un mensaje en una sala de chat.

**Relaciones:**
- Many-to-One con `User` (remitente del mensaje)
- Many-to-One con `ChatRoom` (sala donde se envió)

**Tipos de mensaje (MessageType):**
- `CHAT`: Mensaje de chat normal
- `JOIN`: Usuario se unió a la sala
- `LEAVE`: Usuario abandonó la sala
- `SYSTEM`: Mensaje generado por el sistema
- `FILE`: Mensaje con archivo adjunto
- `IMAGE`: Mensaje con imagen

#### ChatRoom
Representa una sala de chat (privada o grupal).

**Relaciones:**
- Many-to-Many con `User` (participantes)
- One-to-Many con `Message` (mensajes de la sala)
- Many-to-One con `User` (creador de la sala)

**Tipos de sala (ChatRoomType):**
- `PRIVATE`: Chat entre dos usuarios exactamente
- `GROUP`: Chat con múltiples participantes (3+)

### Reglas de Negocio

1. **Salas Privadas:**
    - Exactamente 2 participantes
    - Nombre generado automáticamente
    - Sin descripción ni imagen personalizadas

2. **Salas Grupales:**
    - 3 o más participantes
    - Nombre personalizable y único
    - Pueden tener descripción e imagen
    - Tienen un creador con permisos especiales

3. **Mensajes:**
    - Pueden ser editados (campo `isEdited` e `editedAt`)
    - Soportan múltiples tipos de contenido
    - Timestamp de creación inmutable

4. **Usuarios:**
    - Username y email deben ser únicos
    - Contraseña encriptada (nunca expuesta en API)
    - Estado de conexión actualizable en tiempo real