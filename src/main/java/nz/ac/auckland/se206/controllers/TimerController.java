package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TimerManager;

public class TimerController extends SoundPlayer {

  @FXML private Label timerLabel;

  private final TimerManager timerManager = TimerManager.getInstance();

  // Method to handle the initialization of the timer
  @FXML
  public void initialize() {
    // Configure the timer
    updateTimerLabel();
    if (!timerManager.isTimerRunning()) {
      timerManager.startTimer();
    }

    // Set the guessing start listener
    timerManager.setGuessingStartListener(this::switchToGuessingScene);

    // Set the guess time end listener
    timerManager.setTickListener(this::updateTimerLabel);

    // Play the background music
    if (!timerManager.isTimerRunning()) {
      timerManager.startTimer();
    }
  }

  private void switchToGuessingScene() {
    // Ensure UI updates are performed on the JavaFX application thread
    Platform.runLater(
        () -> {
          GameStateContext context = GameStateContext.getInstance();
          timerManager.setCanGuess(context.canGuess());
          context.setState(context.getGuessingState());
        });

    // Load and switch to the guessing scene
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/guessing.fxml"));

    switchScene(loader);
  }

  private void switchScene(FXMLLoader loader) {
    Platform.runLater(
        () -> {
          try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            App.getStage().setScene(scene);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            App.getStage().show();
          } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions appropriately
          }
        });
  }

  private void updateTimerLabel() {
    // Ensure UI updates are performed on the JavaFX application thread
    Platform.runLater(
        () -> {
          int time = timerManager.getTime();
          if (time >= 0 && timerManager.isCanGuess()) {
            timerLabel.setText(formatTime(time));
            // Change style based on whether it is guess time
            timerLabel.setStyle(
                timerManager.isGuessTime() ? "-fx-text-fill: red;" : "-fx-text-fill: white;");
          } else {
            timerManager.stopTimer();
            // noTime();
          }
        });
  }

  private void noTime() {
    // goes to the loss scene if the player has lost
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/feedback-notime.fxml"));
    Parent root;
    try {
      root = loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // opens the scene
    Scene scene = new Scene(root);
    App.getStage().setScene(scene);
    App.getStage().show();
  }

  private String formatTime(int t) {
    int minutes = t / 60;
    int seconds = t % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  // Add this method if TimerManager supports notifications on each tick
  public void onTimerTick() {
    updateTimerLabel();
  }
}
