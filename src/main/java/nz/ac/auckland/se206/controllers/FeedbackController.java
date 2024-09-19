package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import nz.ac.auckland.se206.util.TimerManager;

public class FeedbackController {

  @FXML private Text responseText;
  @FXML private Text status;
  @FXML private AnchorPane room;
  @FXML private Button playAgainButton;

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

  // Handle the Play Again action
  @FXML
  private void handlePlayAgain(ActionEvent event) {
    System.out.println("Play Again button clicked");

    // Reset the timer
    TimerManager.getInstance().resetTimer();

    // Load the crime scene FXML
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crime-scene.fxml"));
      Parent root = loader.load();

      // Create a new scene
      Scene newScene = new Scene(root);

      // Add CSS to the new scene
      newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

      // Get the current stage
      Stage currentStage = (Stage) playAgainButton.getScene().getWindow();

      // Set the new scene to the stage
      currentStage.setScene(newScene);

      // Show the stage
      currentStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
