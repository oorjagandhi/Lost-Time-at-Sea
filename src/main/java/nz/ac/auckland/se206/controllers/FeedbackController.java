package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TextAnimator;
import nz.ac.auckland.se206.util.TextOutput;
import nz.ac.auckland.se206.util.TimerManager;

public class FeedbackController extends SoundPlayer {

  @FXML private Text responseText;
  @FXML private Text status;
  @FXML private AnchorPane room;
  @FXML private Button playAgainButton;
  @FXML private Text typingText;

  @FXML
  public void initialize() {
    System.out.println("initialize() called in FeedbackController");

    if (typingText != null) {
      System.out.println("typingText is not null");
      startTypingAnimation();
    } else {
      System.out.println("typingText is null");
    }
  }

  private void startTypingAnimation() {
    String fullText = "Analyzing your detective work...";
    int animationTime = 100; // Adjust as needed

    TextOutput textOutput =
        new TextOutput() {
          @Override
          public void writeText(String textOut) {
            Platform.runLater(
                new Runnable() {
                  @Override
                  public void run() {
                    typingText.setText(textOut);
                  }
                });
          }
        };

    TextAnimator textAnimator = new TextAnimator(fullText, animationTime, textOutput);
    Thread thread = new Thread(textAnimator);
    thread.start();
  }

  // Method to update the text
  public void updateResponseText(String text) {
    System.out.println("Updating response text to: " + text);
    responseText.setText(text);
  }

  // Method to update the text
  public void updateStatus(boolean won) {
    System.out.println("Updating response text to: " + won);
    status.setText(won ? "you guessed the correct thief!" : "you guessed the incorrect thief.");
    if (won) {
      playSound("/sounds/congrats.mp3");
      room.setStyle("-fx-background-color: green;");
    } else {
      playSound("/sounds/sad.mp3");
      room.setStyle("-fx-background-color: red;");
    }
  }

  // Handle the Play Again action
  @FXML
  private void onPlayAgain(ActionEvent event) {
    System.out.println("Play Again button clicked");

    // Reset the timer
    TimerManager.getInstance().resetTimer();

    // reset clues
    GameStateContext.getInstance().setClueInteracted(false);

    // reset suspects
    GameStateContext.getInstance().clearSuspects();

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
