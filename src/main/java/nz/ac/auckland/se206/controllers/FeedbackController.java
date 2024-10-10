package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TextAnimator;
import nz.ac.auckland.se206.util.TextOutput;
import nz.ac.auckland.se206.util.TimerManager;

/**
 * Controller class for the feedback view. This class is responsible for displaying the result of
 * the game and handling the play again action.
 */
public class FeedbackController extends SoundPlayer {

  @FXML private Text responseText;
  @FXML private Text status;
  @FXML private AnchorPane room;
  @FXML private ImageView playAgainButton;
  @FXML private Text typingText;

  /** Initializes the feedback view. Starts the typing animation. */
  @FXML
  public void initialize() {
    // debug
    System.out.println("initialize() called in FeedbackController");

    // Check if the typingText is not null
    if (typingText != null) {
      System.out.println("typingText is not null");
      startTypingAnimation();
    } else {
      System.out.println("typingText is null");
    }
  }

  // Starting the typing animation
  private void startTypingAnimation() {
    String fullText = "Analyzing your detective work...";
    int animationTime = 100;

    // writing out the text animation
    TextOutput textOutput =
        new TextOutput() {
          @Override
          public void writeText(String textOut) {
            // sets text to be set later
            Platform.runLater(
                new Runnable() {
                  @Override
                  public void run() {
                    typingText.setText(textOut);
                  }
                });
          }
        };

    // creates a textAnimator instance to animate the text
    TextAnimator textAnimator = new TextAnimator(fullText, animationTime, textOutput);
    Thread thread = new Thread(textAnimator);
    thread.start();
  }

  /**
   * Method to update the response text.
   *
   * @param text the text to update the response text to
   */
  public void updateResponseText(String text) {
    System.out.println("Updating response text to: " + text);
    responseText.setText(text);
  }

  /**
   * Method to update the status text.
   *
   * @param won the result of the game
   */
  public void updateStatus(boolean won) {
    System.out.println("Updating response text to: " + won);
    // Set the status text
    status.setText(won ? "YOU GUESSED THE CORRECT THIEF!" : "YOU GUESSED THE WRONG THIEF");
    // Set the room color based on the result of the game
    if (won) {
      playSound("/sounds/congrats.mp3");
      room.setStyle("-fx-background-color: green;");
    } else {
      playSound("/sounds/sad.mp3");
      room.setStyle("-fx-background-color: red;");
    }
  }

  /**
   * Method to handle the play again button click event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onPlayAgain(MouseEvent event) {
    System.out.println("Play Again button clicked");

    // Reset the timer
    TimerManager.getInstance().resetTimer();

    // reset clues
    GameStateContext.getInstance().setClueInteracted(false, null);

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

  /**
   * Method to handle the mouse enter event.
   *
   * @param event the mouse event
   */
  @FXML
  private void handleMouseEnter(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.HAND); // Change cursor to hand to indicate interactivity
    source.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  /**
   * Method to handle the mouse exit event.
   *
   * @param event the mouse event
   */
  @FXML
  private void handleMouseExit(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.DEFAULT); // Reset cursor
    source.setStyle(""); // Remove the drop shadow effect
  }
}
