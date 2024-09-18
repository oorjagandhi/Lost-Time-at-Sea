package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.util.SoundPlayer;
import nz.ac.auckland.se206.util.TimerManager;

public class TimerController extends SoundPlayer {

  @FXML private Label timerLabel;

  private final TimerManager timerManager = TimerManager.getInstance();

  private MediaPlayer mediaPlayer;

  @FXML
  public void initialize() {
    // Initial label update
    updateTimerLabel();
    // Start the timer if not already running
    if (!timerManager.isTimerRunning()) {
      timerManager.startTimer();
    }

    // Register an update method to be called every second
    timerManager.setTickListener(this::updateTimerLabel);
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
            showYouLoose();
            // playSound("/sounds/outoftime.mp3");
          }
        });
  }

  private void showYouLoose() {
    // goes to the loss scene if the player has lost
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/you_lose.fxml"));
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
