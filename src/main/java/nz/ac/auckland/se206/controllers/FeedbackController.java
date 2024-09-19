// FeedbackController.java
package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class FeedbackController {

  @FXML private Text responseText;
  @FXML private Text status;
  @FXML private AnchorPane room;

  // Method to update the text
  public void updateResponseText(String text) {
    System.out.println("Updating response text to: " + text);
    responseText.setText(text);
  }

  // Method to update the text
  public void updateStatus(boolean won) {
    System.out.println("Updating response text to: " + won);
    status.setText(won ? "You guessed the correct thief!" : "You guessed the incorrect thief.");
    if (won) {
      room.setStyle("-fx-background-color: green;");
    } else {
      room.setStyle("-fx-background-color: red;");
    }
  }
}
