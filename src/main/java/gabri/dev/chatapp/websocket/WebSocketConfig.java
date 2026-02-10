package gabri.dev.chatapp.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket para comunicación en tiempo real.
 * Usa STOMP (Simple Text Oriented Messaging Protocol) sobre WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /**
     * Configura el message broker.
     * - /topic: Para mensajes broadcast (uno a muchos)
     * - /queue: Para mensajes directos (uno a uno)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker simple en memoria
        config.enableSimpleBroker("/topic", "/queue");

        // Prefijo para mensajes desde el cliente al servidor
        config.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes directos a usuarios específicos
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registra los endpoints de WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS(); // Fallback para navegadores que no soportan WebSocket

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor); // Sin SockJS
    }
}