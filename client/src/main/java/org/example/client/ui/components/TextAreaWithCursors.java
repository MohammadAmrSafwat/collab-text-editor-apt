package org.example.client.ui.components;


import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import java.util.HashMap;
import java.util.Map;

public class TextAreaWithCursors extends Pane {
    private final TextArea textArea;
    private final Map<String, Line> remoteCursors = new HashMap<>();
    private final Map<String, Color> userColors = new HashMap<>();

    public TextAreaWithCursors() {
        // Configure main text area
        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: monospace;");

        // Make it fill the available space
        textArea.prefWidthProperty().bind(this.widthProperty());
        textArea.prefHeightProperty().bind(this.heightProperty());

        this.getChildren().add(textArea);

        // Listen to text area changes to update cursor positions
        setupCursorTracking();
    }

    private void setupCursorTracking() {
        // This would be connected to your WebSocket client
        // to send local cursor updates and receive remote ones
    }

    public void updateRemoteCursor(String userId, int position) {
        Platform.runLater(() -> {
            // Get or create cursor for this user
            Line cursor = remoteCursors.computeIfAbsent(userId, id -> {
                Color color = getColorForUser(id);
                Line newCursor = new Line(0, 0, 0, 15);
                newCursor.setStroke(color);
                newCursor.setStrokeWidth(2);
                this.getChildren().add(newCursor);
                return newCursor;
            });

            // Calculate cursor position (simplified)
            // In a real implementation, you'd need precise text metrics
            int row = position / textArea.getPrefColumnCount();
            int col = position % textArea.getPrefColumnCount();

            // Position the cursor (pseudo-code - actual impl needs text layout metrics)
            cursor.setStartX(col * 8 + 5);  // Approximate char width
            cursor.setStartY(row * 20 + 5); // Approximate line height
            cursor.setEndX(col * 8 + 5);
            cursor.setEndY(row * 20 + 20);
        });
    }

    private Color getColorForUser(String userId) {
        return userColors.computeIfAbsent(userId,
                id -> Color.hsb(Math.abs(id.hashCode() % 360), 0.9, 0.9));
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void clearRemoteCursors() {
        this.getChildren().removeAll(remoteCursors.values());
        remoteCursors.clear();
    }
}