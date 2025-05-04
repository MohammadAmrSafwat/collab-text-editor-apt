package com.example.server.controller;


import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CollaborationService collaborationService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               CollaborationService collaborationService) {
        this.messagingTemplate = messagingTemplate;
        this.collaborationService = collaborationService;
    }

    @MessageMapping("/edits/{docId}")
    public void handleEdit(@DestinationVariable String docId,
                           @Payload EditOperation operation) {
        // Process the edit operation using CRDT
        OperationResult result = collaborationService.processEdit(docId, operation);

        // Broadcast to all subscribers
        messagingTemplate.convertAndSend("/topic/edits." + docId, result);
    }

    @MessageMapping("/cursors/{docId}")
    public void handleCursorUpdate(@DestinationVariable String docId,
                                   @Payload CursorPosition position) {
        // Validate and update cursor position
        CursorPosition updated = collaborationService.updateCursor(docId, position);

        // Broadcast to all subscribers
        messagingTemplate.convertAndSend("/topic/cursors." + docId, updated);
    }


}
