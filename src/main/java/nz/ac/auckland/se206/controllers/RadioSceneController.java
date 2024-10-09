package nz.ac.auckland.se206.controllers;

import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/** The RadioSceneController class manages the radio interaction within the game. */
public class RadioSceneController extends ClueSoundController {

  private boolean isPlayingAudio = false;
  private int frequency = 1;
  private MediaPlayer mediaPlayer;
  private Task<Void> backgroundTask;
  private int totalAudios = 5;

  @FXML private ImageView frequencyImage;
  @FXML private ImageView increaseFrequency;
  @FXML private ImageView decreaseFrequency;
  @FXML private ImageView play;
  @FXML protected ImageView currentScene;

  @FXML
  public void initialize() {
    super.initialize();
    if (room != null && currentScene != null) {
      currentScene.setStyle("-fx-effect: dropshadow(gaussian, lightblue, 20, 0.5, 0, 0);");
    }
    if (increaseFrequency != null && decreaseFrequency != null && play != null) {
      increaseFrequency.setCursor(Cursor.HAND);
      decreaseFrequency.setCursor(Cursor.HAND);
      play.setCursor(Cursor.HAND);
    }
    increaseFrequency.setOnMouseClicked(this::handleIncreaseClick);
    decreaseFrequency.setOnMouseClicked(this::handleDecreaseClick);

    // Set up hovering effects for buttons
    play.setOnMouseClicked(this::handlePlayClick);
    increaseFrequency.setOnMouseEntered(this::handleMouseEnter);
    decreaseFrequency.setOnMouseEntered(this::handleMouseEnter);
    play.setOnMouseEntered(this::handleMouseEnter);
    increaseFrequency.setOnMouseExited(this::handleMouseExit);
    decreaseFrequency.setOnMouseExited(this::handleMouseExit);
    play.setOnMouseExited(this::handleMouseExit);
  }

  /**
   * Handles the play button click. Toggles audio playback. If audio is playing, it stops it; if
   * stopped, it plays the audio.
   *
   * @param event The mouse event associated with the button click.
   */
  @FXML
  private void handlePlayClick(MouseEvent event) {
    if (isPlayingAudio) {
      isPlayingAudio = false;
      stopAudio();
    } else {
      isPlayingAudio = true;
      playAudio();
    }
  }

  /**
   * Handles the decrease frequency button click. Changes the frequency to the previous audio file
   * and plays it. If the frequency is less than 1, it wraps around to the last available audio.
   *
   * @param event The mouse event associated with the button click.
   */
  @FXML
  private void handleDecreaseClick(MouseEvent event) {
    stopAudio();
    frequency--;
    if (frequency < 1) {
      frequency = totalAudios;
    }
    frequencyImage.setImage(new Image("/images/clues/sevenseg" + frequency + ".png"));
    playAudio();
  }

  /**
   * Handles the increase frequency button click. Changes the frequency to the next audio file and
   * plays it. If the frequency exceeds the total number of available audios, it wraps around to the
   * first one.
   *
   * @param event The mouse event associated with the button click.
   */
  @FXML
  private void handleIncreaseClick(MouseEvent event) {
    stopAudio();
    frequency++;
    if (frequency > totalAudios) {
      frequency = 1;
    }
    frequencyImage.setImage(new Image("/images/clues/sevenseg" + frequency + ".png"));
    playAudio();
  }

  /** Stops the currently playing audio by canceling the background task that plays the audio. */
  private void stopAudio() {
    if (backgroundTask == null) {
      return;
    }

    isPlayingAudio = false;
    backgroundTask.cancel();
  }

  /**
   * Handles mouse entering a button, changing the cursor to a hand and applying a drop shadow
   * effect to the button.
   *
   * @param event The mouse event associated with entering the button area.
   */
  private void handleMouseEnter(MouseEvent event) {
    ImageView imageView = (ImageView) event.getSource();
    imageView.setCursor(Cursor.HAND);
    imageView.setStyle("-fx-effect: dropshadow(gaussian, yellow, 10, 0.5, 0, 0);");
  }

  /**
   * Handles mouse exiting a button, resetting the cursor to default and removing the drop shadow
   * effect.
   *
   * @param event The mouse event associated with exiting the button area.
   */
  private void handleMouseExit(MouseEvent event) {
    ImageView imageView = (ImageView) event.getSource();
    imageView.setCursor(Cursor.DEFAULT);
    imageView.setStyle("");
  }

  /**
   * Plays the audio corresponding to the current frequency. Audio files are named as radio1.mp3,
   * radio2.mp3, etc. This method plays the audio in the background and can be interrupted by
   * calling stopAudio().
   */
  private void playAudio() {
    String filePath = "/sounds/radio" + frequency + ".mp3";
    backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            URL resource = getClass().getResource(filePath);
            if (resource == null) {
              Platform.runLater(() -> System.out.println("File not found: " + filePath));
              return null;
            }

            Media media = new Media(resource.toString());

            Platform.runLater(
                () -> {
                  mediaPlayer = new MediaPlayer(media);

                  isPlayingAudio = true;

                  // Listener to stop playing when audio ends
                  mediaPlayer
                      .statusProperty()
                      .addListener(
                          (obs, oldStatus, newStatus) -> {
                            if (newStatus == MediaPlayer.Status.STOPPED
                                || newStatus == MediaPlayer.Status.PAUSED) {
                              cancel(); // Cancel task when media stops/pauses
                            }
                          });

                  mediaPlayer.setOnEndOfMedia(
                      () -> {
                        isPlayingAudio = false;
                        System.out.println("Audio finished playing");
                        // Mark that the clue has been interacted with
                        context.setClueInteracted(true, "radio");
                        updateProgressBar();
                      });

                  mediaPlayer.play();
                });

            // Continuously check if the task is cancelled
            while (!isCancelled()) {
              try {
                Thread.sleep(100); // Sleep to avoid busy-waiting
              } catch (InterruptedException e) {
                if (isCancelled()) {
                  break;
                }
              }
            }

            // Stop the audio if the task is cancelled
            Platform.runLater(
                () -> {
                  if (mediaPlayer != null
                      && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.stop();
                    isPlayingAudio = false;
                  }
                });

            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }

  /** Overrides the preBackAction method to stop audio playback before switching scenes. */
  @Override
  protected void preBackAction() {
    stopAudio();
  }

  /** Overrides the preSwitchAction method to stop audio playback before switching scenes. */
  @Override
  protected void preSwitchAction() {
    stopAudio();
  }
}
