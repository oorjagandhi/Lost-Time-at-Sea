package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.util.TextAnimator;
import nz.ac.auckland.se206.util.TextOutput;

/**
 * Controller class for the processing view. This class is responsible for animating the typing text
 * and displaying the result of the API call.
 */
public class ProcessingController {

  @FXML private Text typingText;

  @FXML private Text resultText;

  @FXML private ImageView nextButton;

  private boolean isAnimationDone = false;
  private boolean isApiCallDone = false;
  private String apiResponseContent;
  private boolean userWon;

  @FXML
  public void initialize() {
    startTypingAnimation();
  }

  private void startTypingAnimation() {
    String fullText = "Analyzing your detective work...";
    int animationTime = 10;

    TextOutput textOutput = text -> Platform.runLater(() -> typingText.setText(text));

    TextAnimator textAnimator = new TextAnimator(fullText, animationTime, textOutput);

    Thread animationThread =
        new Thread(
            () -> {
              textAnimator.run();
              // After animation is done
              isAnimationDone = true;
              checkAndProceed();
            });
    animationThread.start();
  }

  /**
   * Sets the result of the API call and whether the user won the game.
   *
   * @param responseContent the response content from the API call
   * @param won whether the user won the game
   */
  public synchronized void setApiCallResult(String responseContent, boolean won) {
    this.apiResponseContent = responseContent;
    this.userWon = won;
    isApiCallDone = true;
    checkAndProceed();
  }

  private synchronized void checkAndProceed() {
    if (isAnimationDone && isApiCallDone) {
      // Both processes are complete; fade in the result text and next button
      Platform.runLater(this::fadeInResultAndButton);
    }
  }

  private void fadeInResultAndButton() {
    // Fade in the result text
    FadeTransition fadeInText = new FadeTransition(Duration.millis(1000), resultText);
    fadeInText.setFromValue(0.0);
    fadeInText.setToValue(1.0);
    fadeInText.play();

    // Fade in the next button
    FadeTransition fadeInButton = new FadeTransition(Duration.millis(1000), nextButton);
    fadeInButton.setFromValue(0.0);
    fadeInButton.setToValue(1.0);
    fadeInButton.play();
  }

  @FXML
  private void onNextClicked() {
    showFeedbackScreen();
  }

  @FXML
  private void handleMouseEnter(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.HAND); // Change cursor to hand to indicate interactivity
    source.setStyle(
        "-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);"); // Apply drop shadow effect
  }

  @FXML
  private void handleMouseExit(MouseEvent event) {
    ImageView source = (ImageView) event.getSource(); // Get the source ImageView
    source.setCursor(Cursor.DEFAULT); // Reset cursor
    source.setStyle(""); // Remove the drop shadow effect
  }

  // Switch to the feedback screen
  private void showFeedbackScreen() {
    try {
      // Load the feedback screen
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/feedback.fxml"));
      Parent feedbackRoot = loader.load();
      FeedbackController feedbackController = loader.getController();

      // Update the response text
      feedbackController.updateResponseText(apiResponseContent);
      feedbackController.updateStatus(userWon);
      Scene feedbackScene = new Scene(feedbackRoot);
      Stage currentStage = (Stage) typingText.getScene().getWindow();
      currentStage.setScene(feedbackScene);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
