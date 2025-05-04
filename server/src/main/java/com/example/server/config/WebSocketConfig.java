package com.example.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages back to clients
        config.enableSimpleBroker(
                "/topic/edits",   // For document edit operations
                "/topic/cursors", // For cursor position updates
                "/topic/presence" // For user presence notifications
        );

        // Designate the "/app" prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");

        // Configure user destination prefix for private messages (not used in this project)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins (adjust for production)
                .withSockJS(); // Enable SockJS for fallback options

        // Add a second endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }


}