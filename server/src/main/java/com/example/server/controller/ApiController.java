package com.example.server.controller;

import com.example.server.model.Document;
import com.example.server.service.CollaborationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final CollaborationService collaborationService;

    public ApiController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    // Create new document
    @PostMapping("/documents")
    public ResponseEntity<String> createDocument() {
        String docId = collaborationService.createNewDocument();
        return ResponseEntity.ok(docId);
    }

    // Import document
    @PostMapping("/documents/import")
    public ResponseEntity<String> importDocument(@RequestBody String content) {
        String docId = collaborationService.importDocument(content);
        return ResponseEntity.ok(docId);
    }

    // Export document
    @GetMapping("/documents/{docId}/export")
    public ResponseEntity<String> exportDocument(@PathVariable String docId) {
        String content = collaborationService.exportDocument(docId);
        return ResponseEntity.ok(content);
    }

    // Generate share codes
    @PostMapping("/documents/{docId}/share-codes")
    public ResponseEntity<ShareCodes> generateShareCodes(
            @PathVariable String docId,
            @RequestParam boolean isEditor) {
        ShareCodes codes = collaborationService.generateShareCodes(docId, isEditor);
        return ResponseEntity.ok(codes);
    }

    // Join session
    @GetMapping("/sessions/{code}")
    public ResponseEntity<SessionInfo> joinSession(@PathVariable String code) {
        SessionInfo sessionInfo = collaborationService.joinSession(code);
        return ResponseEntity.ok(sessionInfo);
    }

    // Record class for share codes
    public record ShareCodes(String viewerCode, String editorCode) {}

    // Record class for session info
    public record SessionInfo(String docId, boolean isEditor, String content) {}
}