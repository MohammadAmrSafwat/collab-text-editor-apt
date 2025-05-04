package org.example.client.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;

public class EditorUI {
    private final Stage primaryStage;
    private final ServerClient serverClient;
    private String currentDocId;
    private boolean isEditor;
    private String userId;

    public EditorUI(Stage primaryStage, ServerClient serverClient) {
        this.primaryStage = primaryStage;
        this.serverClient = serverClient;
        this.userId = "user_" + System.currentTimeMillis(); // Simple unique ID
        showInitialScreen();
    }

    private void showInitialScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Title
        Label title = new Label("Collaborative Text Editor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Action buttons
        Button newDocBtn = new Button("New Document");
        Button importBtn = new Button("Import File");
        Button joinBtn = new Button("Join Session");

        TextField sessionCodeField = new TextField();
        sessionCodeField.setPromptText("Enter session code");
        sessionCodeField.setMaxWidth(200);

        // Button actions
        newDocBtn.setOnAction(e -> createNewDocument());
        importBtn.setOnAction(e -> importDocument());
        joinBtn.setOnAction(e -> joinSession(sessionCodeField.getText()));

        // Layout
        HBox joinBox = new HBox(10, sessionCodeField, joinBtn);
        joinBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, newDocBtn, importBtn, joinBox);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Collaborative Editor");
        primaryStage.show();
    }

    private void createNewDocument() {
        // Create new document on server
        serverClient.createNewDocument(docId -> {
            Platform.runLater(() -> {
                this.currentDocId = docId;
                this.isEditor = true;
                showEditorUI();
                showShareCodes(docId);
            });
        });
    }

    private void importDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                serverClient.importDocument(content, docId -> {
                    Platform.runLater(() -> {
                        this.currentDocId = docId;
                        this.isEditor = true;
                        showEditorUI(content);
                        showShareCodes(docId);
                    });
                });
            } catch (Exception e) {
                showAlert("Error", "Failed to import file: " + e.getMessage());
            }
        }
    }

    private void joinSession(String sessionCode) {
        if (sessionCode == null || sessionCode.trim().isEmpty()) {
            showAlert("Error", "Please enter a session code");
            return;
        }

        serverClient.joinSession(sessionCode, (docId, isEditor, content) -> {
            Platform.runLater(() -> {
                this.currentDocId = docId;
                this.isEditor = isEditor;
                showEditorUI(content);

                if (isEditor) {
                    showShareCodes(docId);
                } else {
                    showAlert("Info", "Joined as viewer (read-only mode)");
                }
            });
        }, error -> {
            Platform.runLater(() -> showAlert("Error", error));
        });
    }

    private void showEditorUI(String initialContent) {
        BorderPane root = new BorderPane();

        // 1. Top Bar - Document info and controls
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));

        Label docLabel = new Label("Document: " + currentDocId.substring(0, 8));
        Button exportBtn = new Button("Export");
        exportBtn.setOnAction(e -> exportDocument());

        topBar.getChildren().addAll(docLabel, exportBtn);

        // 2. Center - Editor Area
        TextAreaWithCursors editor = new TextAreaWithCursors(serverClient, currentDocId, userId, isEditor);
        editor.setInitialContent(initialContent);

        // 3. Right Sidebar - User presence
        VBox userList = new VBox(10);
        userList.setPadding(new Insets(10));
        userList.setStyle("-fx-background-color: #f0f0f0;");

        Label usersLabel = new Label("Active Users");
        usersLabel.setStyle("-fx-font-weight: bold;");
        userList.getChildren().add(usersLabel);

        // Listen for user presence updates
        serverClient.subscribeToPresence(currentDocId, users -> {
            Platform.runLater(() -> {
                userList.getChildren().clear();
                userList.getChildren().add(usersLabel);
                users.forEach(user -> {
                    Label userLabel = new Label(user.getName());
                    userLabel.setStyle("-fx-text-fill: " + user.getColor());
                    userList.getChildren().add(userLabel);
                });
            });
        });

        // Layout
        root.setTop(topBar);
        root.setCenter(editor);
        root.setRight(userList);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private void showShareCodes(String docId) {
        serverClient.getShareCodes(docId, codes -> {
            Platform.runLater(() -> {
                Stage shareStage = new Stage();
                VBox root = new VBox(20);
                root.setPadding(new Insets(20));

                Label title = new Label("Share This Document");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                Label viewerLabel = new Label("Viewer Code (Read-Only)");
                TextField viewerCode = new TextField(codes.getViewerCode());
                viewerCode.setEditable(false);

                Label editorLabel = new Label("Editor Code (Full Access)");
                TextField editorCode = new TextField(codes.getEditorCode());
                editorCode.setEditable(false);

                Button closeBtn = new Button("Close");
                closeBtn.setOnAction(e -> shareStage.close());

                root.getChildren().addAll(title, viewerLabel, viewerCode, editorLabel, editorCode, closeBtn);

                Scene scene = new Scene(root, 300, 250);
                shareStage.setScene(scene);
                shareStage.setTitle("Share Codes");
                shareStage.show();
            });
        });
    }

    private void exportDocument() {
        serverClient.exportDocument(currentDocId, content -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Document");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    Files.write(file.toPath(), content.getBytes());
                    showAlert("Success", "Document exported successfully");
                } catch (Exception e) {
                    showAlert("Error", "Failed to export: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}