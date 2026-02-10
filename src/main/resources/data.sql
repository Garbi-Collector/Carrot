-- Este archivo se ejecuta automáticamente al iniciar la aplicación
-- Inserta usuarios de prueba con contraseñas hasheadas (BCrypt)

-- Contraseña para todos los usuarios de prueba: "password123"
-- Hash BCrypt: $2a$10$rFXKEw7P/eVVXQRQJQqxEO8Z8N8vXQXQJQqxEO8Z8N8vXQXQJQqxEO

-- Insertar usuarios de prueba
INSERT INTO users (username, email, password, full_name, avatar_url, status, enabled, created_at, updated_at)
VALUES
    ('alice', 'alice@chatapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice Johnson', 'https://i.pravatar.cc/150?img=1', 'OFFLINE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bob', 'bob@chatapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob Smith', 'https://i.pravatar.cc/150?img=2', 'OFFLINE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('charlie', 'charlie@chatapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Charlie Brown', 'https://i.pravatar.cc/150?img=3', 'OFFLINE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('diana', 'diana@chatapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Diana Prince', 'https://i.pravatar.cc/150?img=4', 'OFFLINE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('admin', 'admin@chatapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', 'https://i.pravatar.cc/150?img=5', 'OFFLINE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar sala de chat grupal de ejemplo
INSERT INTO chat_rooms (name, type, description, image_url, created_by_user_id, created_at, updated_at)
VALUES
    ('General Chat', 'GROUP', 'Sala de chat general para todos', 'https://i.pravatar.cc/150?img=10', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Developers Team', 'GROUP', 'Chat para el equipo de desarrollo', 'https://i.pravatar.cc/150?img=11', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Random Discussion', 'GROUP', 'Conversaciones aleatorias', 'https://i.pravatar.cc/150?img=12', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Agregar participantes a las salas grupales
-- General Chat: Alice, Bob, Charlie, Diana
INSERT INTO chat_room_participants (chat_room_id, user_id) VALUES
                                                               (1, 1), (1, 2), (1, 3), (1, 4);

-- Developers Team: Alice, Bob, Charlie
INSERT INTO chat_room_participants (chat_room_id, user_id) VALUES
                                                               (2, 1), (2, 2), (2, 3);

-- Random Discussion: Bob, Charlie, Diana, Admin
INSERT INTO chat_room_participants (chat_room_id, user_id) VALUES
                                                               (3, 2), (3, 3), (3, 4), (3, 5);

-- Insertar sala de chat privada de ejemplo (Alice y Bob)
INSERT INTO chat_rooms (name, type, created_by_user_id, created_at, updated_at)
VALUES ('private_1_2', 'PRIVATE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO chat_room_participants (chat_room_id, user_id) VALUES
                                                               (4, 1), (4, 2);

-- Insertar mensajes de ejemplo en General Chat
INSERT INTO messages (content, type, sender_id, chat_room_id, is_edited, sent_at)
VALUES
    ('¡Bienvenidos a General Chat!', 'SYSTEM', 1, 1, false, CURRENT_TIMESTAMP - INTERVAL '2' DAY),
    ('Hola a todos! 👋', 'CHAT', 1, 1, false, CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '1' MINUTE),
    ('Hey Alice! ¿Cómo estás?', 'CHAT', 2, 1, false, CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '2' MINUTE),
    ('Todo bien Bob, gracias! ¿Y tú?', 'CHAT', 1, 1, false, CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '3' MINUTE),
    ('Excelente! Probando esta nueva app de chat', 'CHAT', 2, 1, false, CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '4' MINUTE),
    ('Hola chicos! Me uno a la conversación', 'CHAT', 3, 1, false, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    ('Bienvenido Charlie! 😊', 'CHAT', 4, 1, false, CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '1' MINUTE),
    ('Gracias Diana!', 'CHAT', 3, 1, false, CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '2' MINUTE);

-- Insertar mensajes en Developers Team
INSERT INTO messages (content, type, sender_id, chat_room_id, is_edited, sent_at)
VALUES
    ('Sala creada para el equipo de desarrollo', 'SYSTEM', 1, 2, false, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    ('¿Alguien puede revisar mi PR?', 'CHAT', 2, 2, false, CURRENT_TIMESTAMP - INTERVAL '5' HOUR),
    ('Claro Bob, déjame verlo', 'CHAT', 1, 2, false, CURRENT_TIMESTAMP - INTERVAL '4' HOUR),
    ('Yo también puedo ayudar', 'CHAT', 3, 2, false, CURRENT_TIMESTAMP - INTERVAL '3' HOUR);

-- Insertar mensajes en chat privado Alice-Bob
INSERT INTO messages (content, type, sender_id, chat_room_id, is_edited, sent_at)
VALUES
    ('Hey Bob, ¿tienes un momento?', 'CHAT', 1, 4, false, CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
    ('Sí Alice, dime', 'CHAT', 2, 4, false, CURRENT_TIMESTAMP - INTERVAL '2' HOUR + INTERVAL '30' MINUTE),
    ('Necesito hablar contigo sobre el proyecto', 'CHAT', 1, 4, false, CURRENT_TIMESTAMP - INTERVAL '2' HOUR + INTERVAL '31' MINUTE),
    ('Claro, cuando quieras', 'CHAT', 2, 4, false, CURRENT_TIMESTAMP - INTERVAL '2' HOUR + INTERVAL '32' MINUTE);